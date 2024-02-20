package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.event.StargateTeleportEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import net.knarcraft.stargate.utility.TeleportHelper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * The exit location any entities will be teleported to
     */
    protected Location exit;

    /**
     * The entity being teleported by this teleporter
     */
    protected final Entity teleportedEntity;

    /**
     * Instantiates a new portal teleporter
     *
     * @param portal           <p>The portal which is the target of the teleportation</p>
     * @param teleportedEntity <p>The entity teleported by this teleporter</p>
     */
    public Teleporter(@NotNull Portal portal, @NotNull Entity teleportedEntity) {
        this.portal = portal;
        this.scheduler = Stargate.getInstance().getServer().getScheduler();
        this.teleportedEntity = teleportedEntity;
        this.exit = getExit(teleportedEntity);
    }

    /**
     * Teleports an entity
     *
     * @param origin                <p>The portal the entity teleported from</p>
     * @param stargateTeleportEvent <p>The event to call to make sure the teleportation is valid</p>
     * @return <p>True if the teleportation was successfully performed</p>
     */
    public boolean teleport(@NotNull Portal origin, @Nullable StargateTeleportEvent stargateTeleportEvent) {
        List<Entity> passengers = teleportedEntity.getPassengers();

        //Call the StargateEntityPortalEvent to allow plugins to change destination
        if (!origin.equals(portal) && stargateTeleportEvent != null) {
            exit = triggerPortalEvent(origin, stargateTeleportEvent);
            if (exit == null) {
                return false;
            }
        }

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        if (teleportedEntity.eject()) {
            TeleportHelper.handleEntityPassengers(passengers, teleportedEntity, origin, portal, exit.getDirection(),
                    new Vector());
        }
        teleportedEntity.teleport(exit);
        return true;
    }

    /**
     * Gets the exit location of this teleporter
     *
     * @return <p>The exit location of this teleporter</p>
     */
    @NotNull
    public Location getExit() {
        return exit.clone();
    }

    /**
     * Triggers the entity portal event to allow plugins to change the exit location
     *
     * @param origin                <p>The origin portal teleported from</p>
     * @param stargateTeleportEvent <p>The exit location to teleport the entity to</p>
     * @return <p>The location the entity should be teleported to, or null if the event was cancelled</p>
     */
    @Nullable
    protected Location triggerPortalEvent(@NotNull Portal origin,
                                          @NotNull StargateTeleportEvent stargateTeleportEvent) {
        Stargate.getInstance().getServer().getPluginManager().callEvent((Event) stargateTeleportEvent);
        //Teleport is cancelled. Teleport the entity back to where it came from just for sanity's sake
        if (stargateTeleportEvent.isCancelled()) {
            new EntityTeleporter(origin, teleportedEntity).teleportEntity(origin);
            return null;
        }
        return stargateTeleportEvent.getExit();
    }

    /**
     * Adjusts the rotation of the exit to make the teleporting entity face directly out from the portal
     *
     * @param exit <p>The location the entity will exit from</p>
     */
    protected void adjustExitLocationRotation(@NotNull Location exit) {
        int adjust = 0;
        if (portal.getOptions().isBackwards()) {
            adjust = 180;
        }
        float newYaw = (portal.getYaw() + adjust) % 360;
        Stargate.debug("Portal::adjustRotation", "Setting exit yaw to " + newYaw);
        exit.setDirection(DirectionHelper.getDirectionVectorFromYaw(newYaw));
    }

    /**
     * Loads the chunks outside the portal's entrance
     */
    protected void loadChunks() {
        for (Chunk chunk : getChunksToLoad()) {
            chunk.addPluginChunkTicket(Stargate.getInstance());
            //Allow the chunk to unload after 10 seconds
            Stargate.addChunkUnloadRequest(new ChunkUnloadRequest(chunk, 10000L));
        }
    }

    /**
     * Adjusts the positioning of the portal exit to prevent the given entity from suffocating
     *
     * @param relativeExit <p>The relative exit defined as the portal's exit</p>
     * @param exitLocation <p>The currently calculated portal exit</p>
     * @param entity       <p>The travelling entity</p>
     * @return <p>A location which won't suffocate the entity inside the portal</p>
     */
    @NotNull
    private Location preventExitSuffocation(@NotNull RelativeBlockVector relativeExit,
                                            @NotNull Location exitLocation, @NotNull Entity entity) {
        //Go left to find start of opening
        RelativeBlockVector openingLeft = getPortalExitEdge(relativeExit, -1);

        //Go right to find the end of the opening
        RelativeBlockVector openingRight = getPortalExitEdge(relativeExit, 1);

        //Get the width to check if the entity fits
        int openingWidth = openingRight.right() - openingLeft.right() + 1;
        int existingOffset = relativeExit.right() - openingLeft.right();
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
    @NotNull
    private Location moveExitLocationOutwards(@NotNull Location exitLocation, @NotNull Entity entity) {
        double entitySize = EntityHelper.getEntityMaxSize(entity);
        int entityBoxSize = EntityHelper.getEntityMaxSizeInt(entity);
        if (entitySize > 1) {
            double entityOffset;
            if (portal.getOptions().isAlwaysOn()) {
                entityOffset = (entityBoxSize / 2D);
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
    @NotNull
    private RelativeBlockVector getPortalExitEdge(@NotNull RelativeBlockVector relativeExit, int direction) {
        RelativeBlockVector openingEdge = relativeExit;

        do {
            RelativeBlockVector possibleOpening = new RelativeBlockVector(openingEdge.right() + direction,
                    openingEdge.down(), openingEdge.out());
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
     * @param entity       <p>The travelling entity</p>
     * @param exitLocation <p>The exit location generated</p>
     * @return <p>The location the travelling entity should be teleported to</p>
     */
    @NotNull
    private Location adjustExitLocationHeight(@NotNull Entity entity, @Nullable Location exitLocation) {
        if (exitLocation != null) {
            BlockData blockData = exitLocation.getBlock().getBlockData();
            if ((blockData instanceof Bisected bisected && bisected.getHalf() == Bisected.Half.BOTTOM) ||
                    (blockData instanceof Slab slab && slab.getType() == Slab.Type.BOTTOM) ||
                    blockData.getMaterial() == Material.WATER) {
                //Prevent traveller from spawning inside a slab, or a boat from spawning inside water
                Stargate.debug("adjustExitLocation", "Added a block to get above a slab or a block of water");
                exitLocation.add(0, 1, 0);
            }
            return exitLocation;
        } else {
            Stargate.logWarning("Unable to generate exit location");
            return entity.getLocation();
        }
    }

    /**
     * Gets the exit location for a given entity and current location
     *
     * @param entity <p>The entity to teleport (used to determine distance from portal to avoid suffocation)</p>
     * @return <p>The location the entity should be teleported to.</p>
     */
    @NotNull
    private Location getExit(@NotNull Entity entity) {
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

            double entitySize = EntityHelper.getEntityMaxSize(entity);
            //Prevent exit suffocation for players riding horses or similar
            if (entitySize > 1) {
                exitLocation = preventExitSuffocation(relativeExit, exitLocation, entity);
            }
        } else {
            Stargate.logWarning(String.format("Missing destination point in .gate file %s",
                    portal.getGate().getFilename()));
        }

        //Adjust height and rotation
        Location adjusted = adjustExitLocationHeight(entity, exitLocation);
        adjustExitLocationRotation(adjusted);
        return adjusted;
    }

    /**
     * Gets all relevant chunks near this teleporter's portal's entrance which need to be loaded before teleportation
     *
     * @return <p>A list of chunks to load</p>
     */
    @NotNull
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

}
