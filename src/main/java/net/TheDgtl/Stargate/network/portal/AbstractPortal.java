package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.BypassPermission;
import net.TheDgtl.Stargate.ImportantVersion;
import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
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
import net.TheDgtl.Stargate.util.PortalHelper;
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
    protected Network network;
    protected final String name;
    protected UUID openFor;
    protected Portal destination = null;
    protected Portal overriddenDestination = null;
    protected LineFormatter colorDrawer;

    private long openTime = -1;
    private final UUID ownerUUID;
    private Gate gate;
    private final Set<PortalFlag> flags;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param signBlock <p>The block this portal's sign is located at</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException     <p>If the portal name is invalid</p>
     * @throws NoFormatFoundException <p>If no gate format matches the portal</p>
     * @throws GateConflictException  <p>If the portal's gate conflicts with an existing one</p>
     */
    AbstractPortal(Network network, String name, Block signBlock, Set<PortalFlag> flags, UUID ownerUUID)
            throws NameErrorException, NoFormatFoundException, GateConflictException {
        this.ownerUUID = ownerUUID;
        this.network = network;
        this.name = name;
        this.flags = flags;

        if (!(Tag.WALL_SIGNS.isTagged(signBlock.getType()))) {
            throw new NoFormatFoundException();
        }
        //Get the block behind the sign; the material of that block is stored in a register with available gateFormats
        Directional signDirection = (Directional) signBlock.getBlockData();
        Block behind = signBlock.getRelative(signDirection.getFacing().getOppositeFace());
        List<GateFormat> gateFormats = GateFormat.getPossibleGateFormatsFromControlBlockMaterial(behind.getType());
        setGate(findMatchingGate(gateFormats, signBlock.getLocation(), signDirection.getFacing()));

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

        AbstractPortal.portalCount++;
        AbstractPortal.allUsedFlags.addAll(flags);
    }

    @Override
    public List<Location> getSignLocations() {
        return gate.getSignLocations();
    }

    @Override
    public void update() {
        if (isOpen() && this.overriddenDestination == null && network.getPortal(getDestination().getName()) == null) {
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

    @Override
    public void close(boolean forceClose) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !forceClose)
            return;
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
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(Network targetNetwork) {
        this.network = targetNetwork;
        this.drawControlMechanisms();
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {

        BlockFace portalFacing = gate.getSignFace().getOppositeFace();
        if (flags.contains(PortalFlag.BACKWARDS)) {
            portalFacing = portalFacing.getOppositeFace();
        }

        BlockFace entranceFace = null;
        int useCost = 0;
        if (origin != null) {
            //If player enters from back, then take that into consideration
            entranceFace = origin.getGate().getFacing();
            Vector vector = origin.getGate().getRelativeVector(target.getLocation().add(new Vector(-0.5, 0, -0.5)));
            if (vector.getX() > 0) {
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
        for (Location location : this.getSignLocations()) {
            Sign sign = (Sign) location.getBlock().getState();
            if (color != null) {
                sign.setColor(color);
                sign.update();
            }
            if (!VersionParser.bukkitIsNewerThan(ImportantVersion.NO_CHAT_COLOR_IMPLEMENTED))
                colorDrawer = new NoLineColorFormatter();
            else {
                colorDrawer = new LineColorFormatter(sign.getColor(), sign.getType());
            }
            this.drawControlMechanisms();
        }
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
                Stargate.factory.unRegisterLocation(formatType, loc);
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

    /**
     * Sets the gate used by this portal
     *
     * @param gate <p>The gate to be used by this portal</p>
     */
    private void setGate(Gate gate) {
        this.gate = gate;
    }

    /**
     * Tries to find a gate at the given location matching one of the given gate formats
     *
     * @param gateFormats  <p>The gate formats to look for</p>
     * @param signLocation <p>The location of the sign of the portal to look for</p>
     * @param signFacing   <p>The direction the sign is facing</p>
     * @return <p>A gate if found, or null if no gate was found</p>
     * @throws NoFormatFoundException <p>If no gate was found at the given location matching any of the given formats</p>
     * @throws GateConflictException  <p>If the found gate conflicts with another gate</p>
     */
    private Gate findMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
            throws NoFormatFoundException, GateConflictException {
        Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
        for (GateFormat gateFormat : gateFormats) {
            Stargate.log(Level.FINE, "--------- " + gateFormat.getFileName() + " ---------");
            try {
                return new Gate(gateFormat, signLocation, signFacing, flags);
            } catch (InvalidStructureException ignored) {
            }
        }
        throw new NoFormatFoundException();
    }

}