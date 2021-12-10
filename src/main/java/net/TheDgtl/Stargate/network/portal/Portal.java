package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.event.StargateOpenEvent;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.formatting.LineColorFormatter;
import net.TheDgtl.Stargate.network.portal.formatting.LineFormatter;
import net.TheDgtl.Stargate.network.portal.formatting.NoLineColorFormatter;
import net.TheDgtl.Stargate.util.VersionParser;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The parent class for every portal that interacts with server worlds
 *
 * @author Thorin
 */
public abstract class Portal implements IPortal {
    /**
     * Used in bstats metrics
     */
    public static int portalCount = 0;
    /**
     * Used for bstats metrics, this is every flag that has been used by all portals
     */
    public static EnumSet<PortalFlag> allUsedFlags = EnumSet.noneOf(PortalFlag.class);
    /**
     *
     */
    protected Network network;
    /**
     * Behaviours: - Cycle through PortalStates, make current state listener for
     * movements - (Constructor) Check validity, write sign, add self to a list in
     * the network
     * <p>
     * Added behaviours - (Listener) Listen for stargate clock (maybe 1 tick per
     * minute or something) maybe follow an external script that gives when the
     * states should change
     */
    final int openDelay = 20; // seconds
    private Gate gate;
    private final Set<PortalFlag> flags;
    final String name;
    UUID openFor;
    IPortal destination = null;
    IPortal overRideDestination = null;
    private long openTime = -1;
    private final UUID ownerUUID;
    protected LineFormatter colorDrawer;


    Portal(Network network, String name, Block sign, Set<PortalFlag> flags, UUID ownerUUID)
            throws NameErrorException, NoFormatFoundException, GateConflictException {
        this.ownerUUID = ownerUUID;
        this.network = network;
        this.name = name;
        this.flags = flags;

        if (!(Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            throw new NoFormatFoundException();
        }
        /*
         * Get the block behind the sign; the material of that block is stored in a
         * register with available gateFormats
         */
        Directional signDirection = (Directional) sign.getBlockData();
        Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
        List<GateFormat> gateFormats = GateFormat.getPossibleGateFormatsFromControlBlockMaterial(behind.getType());
        setGate(FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing()));

        if (name.trim().isEmpty() || (name.length() >= Stargate.MAX_TEXT_LENGTH))
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        if (this.network.isPortalNameTaken(name)) {
            throw new NameErrorException(TranslatableMessage.ALREADY_EXIST);
        }

        setSignColor(null);

        if (gate.getFormat().isIronDoorBlockable) {
            flags.add(PortalFlag.IRON_DOOR);
        }

        StringBuilder msg = new StringBuilder("Selected with flags ");
        for (PortalFlag flag : flags) {
            msg.append(flag.getCharacterRepresentation());
        }
        Stargate.log(Level.FINE, msg.toString());

        if (hasFlag(PortalFlag.ALWAYS_ON))
            this.open(null);

        Portal.portalCount++;
        Portal.allUsedFlags.addAll(flags);
    }

    /**
     * Look through every stored gateFormat, checks every possible rotation / flip
     *
     * @param gateFormats
     * @param signLocation
     * @param signFacing
     * @return A gate with matching format
     * @throws NoFormatFoundException
     * @throws GateConflictException
     */
    private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
            throws NoFormatFoundException, GateConflictException {
        Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
        for (GateFormat gateFormat : gateFormats) {
            Stargate.log(Level.FINE, "--------- " + gateFormat.name + " ---------");
            try {
                return new Gate(gateFormat, signLocation, signFacing, this);
            } catch (InvalidStructureException ignored) {
            }
        }
        throw new NoFormatFoundException();
    }

    public HashMap<BlockLocation, Portal> generateLocationHashMap(List<BlockLocation> locations) {
        HashMap<BlockLocation, Portal> output = new HashMap<>();
        for (BlockLocation loc : locations) {
            output.put(loc, this);
        }
        return output;
    }

    @Override
    public Location getSignLocation() {
        return gate.getSignLocation();
    }

    /**
     * Replacement function for {@link Sign#setColor(org.bukkit.DyeColor)}, as the portal sign is an interface
     * that is using a combination of various colors; more has to be processed
     *
     * @param color <p> Color to change the sign text to. If nulled, then default color will be used </p>
     */
    public void setSignColor(DyeColor color) {
        Sign sign = (Sign) this.getSignLocation().getBlock().getState();
        if (color != null) {
            sign.setColor(color);
            sign.update();
        }
        if (!VersionParser.bukkitIsNewerThan(VersionParser.ImportantVersion.NO_CHATCOLOR_IMPLEMENTED))
            colorDrawer = new NoLineColorFormatter();
        else {
            colorDrawer = new LineColorFormatter(sign.getColor(), sign.getType());
        }
        this.drawControlMechanism();
    }

    @Override
    public void update() {
        if (isOpen() && this.overRideDestination == null && network.getPortal(getDestination().getName()) == null) {
            close(false);
        }
        drawControlMechanism();
    }

    public void onSignClick(PlayerInteractEvent event) {
    }

    public abstract void drawControlMechanism();

    public abstract IPortal loadDestination();

    public boolean isOpen() {
        return getGate().isOpen();
    }

    public Set<PortalFlag> getFlags() {
        return flags;
    }

    /**
     * Remove all information stored on this gate
     */
    public void destroy() {
        close(true);
        this.network.removePortal(this, true);
        String[] lines = new String[]{name, "", "", ""};
        getGate().drawControlMechanism(lines, false);

        for (GateStructureType formatType : GateStructureType.values()) {
            for (BlockLocation loc : this.getGate().getLocations(formatType)) {
                Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                network.unRegisterLocation(formatType, loc);
            }
        }
        network.updatePortals();
    }

    public void open(Player actor) {
        getGate().open();
        if (actor != null)
            this.openFor = actor.getUniqueId();
        if (hasFlag(PortalFlag.ALWAYS_ON)) {
            return;
        }
        long openTime = System.currentTimeMillis();
        this.openTime = openTime;


        // Create action which will close this portal
        Supplier<Boolean> action = () -> {
            close(openTime);
            return true;
        };

        // Make the action on a delay
        Stargate.syncSecPopulator.addAction(new DelayedAction(openDelay, action));
    }

    /**
     * Everytime most of the portals opens, there is going to be a scheduled event
     * to close it after a specific time. If a player enters the portal before this,
     * then it is going to close, but the scheduled close event is still going to be
     * there. And if the portal gets activated again, it is going to close
     * prematurely, because of this already scheduled event. Solution to avoid this
     * is to assign an open-time for each scheduled close event and only close if the
     * related open time matches with the most recent time the portal was opened.
     *
     * @param relatedOpenTime
     */
    public void close(long relatedOpenTime) {
        if (relatedOpenTime == openTime)
            close(false);
    }

    @Override
    public void close(boolean forceClose) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !forceClose)
            return;
        getGate().close();
        drawControlMechanism();
        openFor = null;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return ((openFor == null) || (target.getUniqueId() == openFor));
    }

    public Location getExit() {
        return gate.getExit();
    }

    public void overrideDestination(IPortal destination) {
        this.overRideDestination = destination;
    }

    public Network getNetwork() {
        return this.network;
    }

    public void setNetwork(Network targetNetwork) {
        this.network = targetNetwork;
        this.drawControlMechanism();
    }

    protected IPortal getDestination() {
        if (overRideDestination == null)
            return destination;
        return overRideDestination;
    }

    public void onButtonClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.hasFlag(PortalFlag.IRON_DOOR) && event.useInteractedBlock() == Result.DENY) {
            Block exitBlock = gate.getExit().add(gate.getFacing().getDirection()).getBlock();
            if (exitBlock.getType() == Material.IRON_DOOR) {
                Directional signDirection = (Directional) gate.getSignLocation().getBlock().getBlockData();
                Directional doorDirection = (Directional) exitBlock.getBlockData();
                if (signDirection.getFacing() == doorDirection.getFacing()) {
                    return;
                }
            }
        }

        IPortal destination = loadDestination();
        if (destination == null) {
            player.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.INVALID));
            return;
        }
        PermissionManager permissionManager = new PermissionManager(player);
        StargateOpenEvent oEvent = new StargateOpenEvent(player, this, false);
        if (!permissionManager.hasPermission(oEvent)) {
            event.getPlayer().sendMessage(permissionManager.getDenyMessage());
            return;
        }
        if (oEvent.isCancelled()) {
            return;
        }

        this.destination = destination;
        open(player);
        destination.open(player);
    }

    public void onIrisEntrance(Entity target) {
        Stargate.log(Level.FINEST, "Trying to teleport entity, initial velocity: " + target.getVelocity());
        doTeleport(target);
    }

    public Gate getGate() {
        return gate;
    }

    public void setGate(Gate gate) {
        this.gate = gate;
    }

    @Override
    public void teleportHere(Entity target, Portal origin) {

        BlockFace portalFacing = gate.getSignFace().getOppositeFace();
        if (flags.contains(PortalFlag.BACKWARDS))
            portalFacing = portalFacing.getOppositeFace();


        BlockFace enterFacing = null;
        int useCost = 0;
        if (origin != null) {
            /*
             * If player enters from back, then take that into consideration
             */
            enterFacing = origin.getGate().getFacing();
            Vector vec = origin.getGate().getRelativeVector(target.getLocation().add(new Vector(-0.5, 0, -0.5)));
            if (vec.getX() > 0) {
                enterFacing = enterFacing.getOppositeFace();
            }

            boolean shouldCharge = !(this.hasFlag(PortalFlag.FREE) || origin.hasFlag(PortalFlag.FREE))
                    && target instanceof Player && !target.hasPermission(BypassPermission.COST_USE.getPermissionString());
            useCost = shouldCharge ? Settings.getInteger(Setting.USE_COST) : 0;
        }

        Teleporter teleporter = new Teleporter(getExit(), origin, portalFacing, enterFacing, useCost,
                TranslatableMessage.TELEPORT, true);

        Supplier<Boolean> action = () -> {
            teleporter.teleport(target);
            return true;
        };
        Stargate.syncTickPopulator.addAction(new SupplierAction(action));

    }

    @Override
    public void doTeleport(Entity target) {
        IPortal destination = getDestination();
        if (destination == null) {
            Teleporter teleporter = new Teleporter(this.getExit(), this, gate.getFacing(), gate.getFacing(), 0, TranslatableMessage.INVALID, false);
            teleporter.teleport(target);
            return;
        }

        destination.teleportHere(target, this);
        destination.close(false);
        close(false);
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean hasFlag(PortalFlag flag) {
        return flags.contains(flag);
    }

    public String getAllFlagsString() {
        StringBuilder out = new StringBuilder();
        for (PortalFlag flag : flags) {
            out.append(flag.getCharacterRepresentation());
        }
        return out.toString();
    }

    public String getDesignName() {
        return gate.getFormat().name;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isOwner(Player player) {
        return ownerUUID.equals(player.getUniqueId());
    }
}