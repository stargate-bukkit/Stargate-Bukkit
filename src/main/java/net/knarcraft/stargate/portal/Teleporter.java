package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * The portal teleporter takes care of common teleportation logic
 */
public abstract class Teleporter {

    /**
     * The portal the entity is teleporting to
     */
    protected final Portal portal;
    /**
     * The scheduler to use for delaying tasks
     */
    protected final BukkitScheduler scheduler;

    /**
     * Instantiates a new portal teleporter
     *
     * @param portal <p>The portal which is the target of the teleportation</p>
     */
    public Teleporter(Portal portal) {
        this.portal = portal;
        this.scheduler = Stargate.getInstance().getServer().getScheduler();
    }


    /**
     * Adjusts the rotation of the exit to make the teleporting entity face directly out from the portal
     *
     * @param exit <p>The location the entity will exit from</p>
     */
    protected void adjustRotation(Location exit) {
        int adjust = 0;
        if (portal.getOptions().isBackwards()) {
            adjust = 180;
        }
        float newYaw = (portal.getYaw() + adjust) % 360;
        Stargate.debug("Portal::adjustRotation", "Setting exit yaw to " + newYaw);
        exit.setYaw(newYaw);
    }

    /**
     * Gets the exit location for a given entity and current location
     *
     * @param entity    <p>The entity to teleport (used to determine distance from portal to avoid suffocation)</p>
     * @param traveller <p>The location of the entity travelling</p>
     * @return <p>The location the entity should be teleported to.</p>
     */
    public Location getExit(Entity entity, Location traveller) {
        Location exitLocation = null;
        RelativeBlockVector relativeExit = portal.getGate().getLayout().getExit();
        if (relativeExit != null) {
            BlockLocation exit = portal.getBlockAt(relativeExit);

            //Move one block out to prevent exiting inside the portal
            float portalYaw = portal.getYaw();
            if (portal.getOptions().isBackwards()) {
                portalYaw += 180;
            }
            exitLocation = exit.getRelativeLocation(0D, 0D, 1, portalYaw);

            if (entity != null) {
                double entitySize = EntityHelper.getEntityMaxSize(entity);
                //Prevent exit suffocation for players riding horses or similar
                if (entitySize > 1) {
                    exitLocation = preventExitSuffocation(relativeExit, exitLocation, entity);
                }
            }
        } else {
            Stargate.logWarning(String.format("Missing destination point in .gate file %s",
                    portal.getGate().getFilename()));
        }

        //Adjust pitch and height
        return adjustExitLocation(traveller, exitLocation);
    }

    /**
     * Adjusts the positioning of the portal exit to prevent the given entity from suffocating
     *
     * @param relativeExit <p>The relative exit defined as the portal's exit</p>
     * @param exitLocation <p>The currently calculated portal exit</p>
     * @param entity       <p>The travelling entity</p>
     * @return <p>A location which won't suffocate the entity inside the portal</p>
     */
    private Location preventExitSuffocation(RelativeBlockVector relativeExit, Location exitLocation, Entity entity) {
        //Go left to find start of opening
        RelativeBlockVector openingLeft = getPortalExitEdge(relativeExit, -1);

        //Go right to find the end of the opening
        RelativeBlockVector openingRight = getPortalExitEdge(relativeExit, 1);

        //Get the width to check if the entity fits
        int openingWidth = openingRight.getRight() - openingLeft.getRight() + 1;
        int existingOffset = relativeExit.getRight() - openingLeft.getRight();
        double newOffset = (openingWidth - existingOffset) / 2D;

        //Remove the half offset for better centering
        if (openingWidth > 1) {
            newOffset -= 0.5;
        }
        exitLocation = DirectionHelper.moveLocation(exitLocation, newOffset, 0, 0, portal.getYaw());

        //Move large entities further from the portal
        return moveExitLocationOutwards(exitLocation, entity);
    }

    /**
     * Moves the exit location out from the portal to prevent the entity from entering a teleportation loop
     *
     * @param exitLocation <p>The current exit location to adjust</p>
     * @param entity       <p>The entity to adjust the exit location for</p>
     * @return <p>The adjusted exit location</p>
     */
    private Location moveExitLocationOutwards(Location exitLocation, Entity entity) {
        double entitySize = EntityHelper.getEntityMaxSize(entity);
        int entityBoxSize = EntityHelper.getEntityMaxSizeInt(entity);
        if (entitySize > 1) {
            double entityOffset;
            if (portal.getOptions().isAlwaysOn()) {
                entityOffset = entityBoxSize / 2D;
            } else {
                entityOffset = (entitySize / 2D) - 1;
            }
            //If a horse has a player riding it, the player will spawn inside the roof of a standard portal unless it's 
            // moved one block out.
            if (entity instanceof AbstractHorse) {
                entityOffset += 1;
            }
            exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0, entityOffset, portal.getYaw());
        }
        return exitLocation;
    }

    /**
     * Gets one of the edges of a portal's opening/exit
     *
     * @param relativeExit <p>The known exit to start from</p>
     * @param direction    <p>The direction to move (+1 for right, -1 for left)</p>
     * @return <p>The right or left edge of the opening</p>
     */
    private RelativeBlockVector getPortalExitEdge(RelativeBlockVector relativeExit, int direction) {
        RelativeBlockVector openingEdge = relativeExit;

        do {
            RelativeBlockVector possibleOpening = new RelativeBlockVector(openingEdge.getRight() + direction,
                    openingEdge.getDown(), openingEdge.getOut());
            if (portal.getGate().getLayout().getExits().contains(possibleOpening)) {
                openingEdge = possibleOpening;
            } else {
                break;
            }
        } while (true);

        return openingEdge;
    }

    /**
     * Adjusts an exit location by setting pitch and adjusting height
     *
     * <p>If the exit location is a slab or water, the exit location will be changed to arrive one block above. The
     * slab check is necessary to prevent the player from clipping through the slab and spawning beneath it. The water
     * check is necessary when teleporting boats to prevent it from becoming a submarine.</p>
     *
     * @param traveller    <p>The location of the travelling entity</p>
     * @param exitLocation <p>The exit location generated</p>
     * @return <p>The location the travelling entity should be teleported to</p>
     */
    private Location adjustExitLocation(Location traveller, Location exitLocation) {
        if (exitLocation != null) {
            BlockData blockData = portal.getWorld().getBlockAt(exitLocation).getBlockData();
            if ((blockData instanceof Bisected && ((Bisected) blockData).getHalf() == Bisected.Half.BOTTOM) ||
                    (blockData instanceof Slab) && ((Slab) blockData).getType() == Slab.Type.BOTTOM) {
                //Prevent traveller from spawning inside a slab
                Stargate.debug("adjustExitLocation", "Added a block to get above a slab");
                exitLocation.add(0, 1, 0);
            } else if (blockData.getMaterial() == Material.WATER) {
                //If there's water outside, go one up to allow for boat teleportation
                Stargate.debug("adjustExitLocation", "Added a block to get above a block of water");
                exitLocation.add(0, 1, 0);
            }

            exitLocation.setPitch(traveller.getPitch());
            return exitLocation;
        } else {
            Stargate.logWarning("Unable to generate exit location");
            return traveller;
        }
    }

    /**
     * Loads the chunks outside the portal's entrance
     */
    protected void loadChunks() {
        for (Chunk chunk : getChunksToLoad()) {
            chunk.addPluginChunkTicket(Stargate.getInstance());
            //Allow the chunk to unload after 3 seconds
            Stargate.addChunkUnloadRequest(new ChunkUnloadRequest(chunk, 3000L));
        }
    }

    /**
     * Gets all relevant chunks near this teleporter's portal's entrance which need to be loaded before teleportation
     *
     * @return <p>A list of chunks to load</p>
     */
    private List<Chunk> getChunksToLoad() {
        List<Chunk> chunksToLoad = new ArrayList<>();
        for (RelativeBlockVector vector : portal.getGate().getLayout().getEntrances()) {
            BlockLocation entranceLocation = portal.getBlockAt(vector);
            Chunk chunk = entranceLocation.getChunk();
            //Make sure not to load chunks twice
            if (!chunksToLoad.contains(chunk)) {
                chunksToLoad.add(chunk);
            }

            //Get the chunk in front of the gate entrance
            int blockOffset = portal.getOptions().isBackwards() ? -5 : 5;
            Location fiveBlocksForward = DirectionHelper.moveLocation(entranceLocation, 0, 0, blockOffset,
                    portal.getYaw());
            //Load the chunk five blocks forward to make sure the teleported entity will never spawn in unloaded chunks
            Chunk forwardChunk = fiveBlocksForward.getChunk();
            if (!chunksToLoad.contains(forwardChunk)) {
                chunksToLoad.add(forwardChunk);
            }
        }
        return chunksToLoad;
    }

    /**
     * Teleports any creatures leashed by the player
     *
     * @param player <p>The player which is teleported</p>
     * @param origin <p>The portal the player is teleporting from</p>
     */
    protected void teleportLeashedCreatures(Player player, Portal origin) {
        //If this feature is disabled, just return
        if (!Stargate.getGateConfig().handleLeashedCreatures()) {
            return;
        }

        //Find any nearby leashed entities to teleport with the player
        List<Creature> nearbyEntities = getLeashedCreatures(player);
        //Teleport all creatures leashed by the player to the portal the player is to exit from
        for (Creature creature : nearbyEntities) {
            creature.setLeashHolder(null);
            scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
                new EntityTeleporter(portal, creature).teleport(origin);
                scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> creature.setLeashHolder(player), 3);
            }, 2);
        }
    }

    /**
     * Gets all creatures leashed by a player within the given range
     *
     * @param player <p>The player to check</p>
     * @return <p>A list of all creatures the player is holding in a leash (lead)</p>
     */
    protected List<Creature> getLeashedCreatures(Player player) {
        List<Creature> leashedCreatures = new ArrayList<>();
        //Find any nearby leashed entities to teleport with the player
        List<Entity> nearbyEntities = player.getNearbyEntities(15, 15, 15);
        //Teleport all creatures leashed by the player to the portal the player is to exit from
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Creature creature && creature.isLeashed() && creature.getLeashHolder() == player) {
                leashedCreatures.add(creature);
            }
        }
        return leashedCreatures;
    }

}
