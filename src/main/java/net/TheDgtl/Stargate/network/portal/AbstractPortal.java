package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.ImportantVersion;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.event.StargateOpenEvent;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.NetworkAPI;
import net.TheDgtl.Stargate.network.portal.formatting.LineColorFormatter;
import net.TheDgtl.Stargate.network.portal.formatting.LineFormatter;
import net.TheDgtl.Stargate.network.portal.formatting.NoLineColorFormatter;
import net.TheDgtl.Stargate.util.PortalHelper;
import net.TheDgtl.Stargate.util.VersionParser;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * An abstract implementation of a real portal
 *
 * @author Thorin
 */
public abstract class AbstractPortal implements RealPortal {
    /**
     * Used in bStats metrics
     */
    public static int portalCount = 0;
    /**
     * Used for bStats metrics, this is every flag that has been used by all portals
     */
    public static final Set<PortalFlag> allUsedFlags = EnumSet.noneOf(PortalFlag.class);

    protected final int openDelay = 20;
    protected NetworkAPI network;
    protected final String name;
    protected UUID openFor;
    protected Portal destination = null;
    protected Portal overriddenDestination = null;
    protected LineFormatter colorDrawer;

    private long openTime = -1;
    private UUID ownerUUID;
    private final Gate gate;
    private final Set<PortalFlag> flags;
    private final StargateLogger logger;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    AbstractPortal(NetworkAPI network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, StargateLogger logger)
            throws NameErrorException {
        this.ownerUUID = ownerUUID;
        this.network = network;
        this.name = name;
        this.flags = flags;
        this.gate = gate;
        this.logger = logger;

        if (name.trim().isEmpty() || (name.length() >= Stargate.MAX_TEXT_LENGTH)) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        } else if (this.network.isPortalNameTaken(name)) {
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

        if (hasFlag(PortalFlag.ALWAYS_ON)) {
            this.open(null);
        }

        AbstractPortal.portalCount++;
        AbstractPortal.allUsedFlags.addAll(flags);
    }

    @Override
    public List<Location> getPortalPosition(PositionType type) {
        return gate.getPortalPositions(type);
    }

    @Override
    public void update() {
        if (isOpen() && this.overriddenDestination == null && getDestination() == null) {
            close(false);
        }
        drawControlMechanisms();
    }

    @Override
    public boolean isOpen() {
        return getGate().isOpen();
    }

    @Override
    public void open(Player actor) {
        getGate().open();
        if (actor != null) {
            this.openFor = actor.getUniqueId();
        }
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

    @Override
    public void close(boolean forceClose) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !forceClose) {
            return;
        }
        getGate().close();
        drawControlMechanisms();
        openFor = null;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return ((openFor == null) || (target.getUniqueId() == openFor));
    }

    @Override
    public void overrideDestination(Portal destination) {
        this.overriddenDestination = destination;
    }

    @Override
    public NetworkAPI getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(Network targetNetwork) throws NameErrorException {
        if(targetNetwork.getPortal(this.name) != null)
            throw new NameErrorException(null);
        this.network = targetNetwork;
        //TODO: update network in database
        this.drawControlMechanisms();
    }

    @Override
    //TODO: Finish implementation (modify database).
    public void setOwner(UUID targetPlayer) {
        this.ownerUUID = targetPlayer;
    }


    @Override
    public void teleportHere(Entity target, RealPortal origin) {

        BlockFace portalFacing = gate.getFacing().getOppositeFace();
        if (flags.contains(PortalFlag.BACKWARDS)) {
            portalFacing = portalFacing.getOppositeFace();
        }

        BlockFace entranceFace = null;
        int useCost = 0;
        if (origin != null) {
            //If player enters from back, then take that into consideration
            entranceFace = origin.getGate().getFacing();
            Vector vector = origin.getGate().getRelativeVector(target.getLocation().add(new Vector(-0.5, 0, -0.5)));
            if (vector.getX() < 0) {
                entranceFace = entranceFace.getOppositeFace();
            }

            boolean shouldCharge = !(this.hasFlag(PortalFlag.FREE) || origin.hasFlag(PortalFlag.FREE))
                    && target instanceof Player && !target.hasPermission(BypassPermission.COST_USE.getPermissionString());
            useCost = shouldCharge ? Settings.getInteger(Setting.USE_COST) : 0;
        }

        Teleporter teleporter = new Teleporter(getExit(), origin, portalFacing, entranceFace, useCost,
                TranslatableMessage.TELEPORT, true);

        Stargate.syncTickPopulator.addAction(new SupplierAction(() -> {
            teleporter.teleport(target);
            return true;
        }));
    }

    @Override
    public void doTeleport(Entity target) {
        Portal destination = getDestination();
        if (destination == null) {
            Teleporter teleporter = new Teleporter(this.getExit(), this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, TranslatableMessage.INVALID, false);
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

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String getAllFlagsString() {
        return PortalHelper.flagsToString(flags);
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void onButtonClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.hasFlag(PortalFlag.IRON_DOOR) && event.useInteractedBlock() == Result.DENY) {
            Block exitBlock = gate.getExit().add(gate.getFacing().getDirection()).getBlock();
            if (exitBlock.getType() == Material.IRON_DOOR) {
                BlockFace gateDirection = gate.getFacing();
                Directional doorDirection = (Directional) exitBlock.getBlockData();
                if (gateDirection == doorDirection.getFacing()) {
                    return;
                }
            }
        }

        Portal destination = loadDestination();
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

    @Override
    public void setSignColor(DyeColor color) {
        colorDrawer = new NoLineColorFormatter();
        for (Location location : this.getPortalPosition(PositionType.SIGN)) {
            if (!(location.getBlock().getState() instanceof Sign)) {
                logger.logMessage(Level.WARNING, String.format("Could not find a sign for portal %s in network %s \n"
                                + "This is most likely caused from a bug // please contact developers (use ''sg about'' for github repo)",
                        this.name, this.network.getName()));
                continue;
            }
            Sign sign = (Sign) location.getBlock().getState();
            if (color != null) {
                sign.setColor(color);
                sign.update();
            }
            if (VersionParser.bukkitIsNewerThan(ImportantVersion.NO_CHAT_COLOR_IMPLEMENTED)) {
                colorDrawer = new LineColorFormatter(sign.getColor(), sign.getType());
            }
        }
        this.drawControlMechanisms();
    }

    @Override
    public Gate getGate() {
        return gate;
    }

    @Override
    public void destroy() {
        close(true);
        this.network.removePortal(this, true);
        String[] lines = new String[]{name, "", "", ""};
        getGate().drawControlMechanisms(lines, false);

        for (GateStructureType formatType : GateStructureType.values()) {
            for (BlockLocation loc : this.getGate().getLocations(formatType)) {
                Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                Stargate.getRegistry().unRegisterLocation(formatType, loc);
            }
        }
        network.updatePortals();
    }

    @Override
    public void close(long relatedOpenTime) {
        if (relatedOpenTime == openTime) {
            close(false);
        }
    }

    @Override
    public Location getExit() {
        return gate.getExit();
    }

    /**
     * Gets this portal's current destination
     *
     * @return <p>This portal's current destination</p>
     */
    private Portal getDestination() {
        if (overriddenDestination == null) {
            return destination;
        } else {
            return overriddenDestination;
        }
    }

}