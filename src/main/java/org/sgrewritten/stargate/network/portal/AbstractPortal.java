package org.sgrewritten.stargate.network.portal;

import org.bukkit.Bukkit;
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
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.action.DelayedAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.event.StargateAccessEvent;
import org.sgrewritten.stargate.event.StargateCloseEvent;
import org.sgrewritten.stargate.event.StargateDeactivateEvent;
import org.sgrewritten.stargate.event.StargateOpenEvent;
import org.sgrewritten.stargate.event.StargateSignFormatEvent;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.manager.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.portal.formatting.LegacyLineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.network.portal.formatting.NoLineColorFormatter;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.property.NonLegacyMethod;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.util.ArrayList;
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
    private UUID ownerUUID;
    private final Gate gate;
    private final Set<PortalFlag> flags;
    protected final StargateLogger logger;

    protected long activatedTime;
    protected UUID activator;
    protected boolean isDestroyed = false;
    private static final int ACTIVE_DELAY = 15;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    AbstractPortal(Network network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, StargateLogger logger)
            throws NameErrorException {
        this.ownerUUID = ownerUUID;
        this.network = network;
        this.name = name;
        this.flags = flags;
        this.gate = gate;
        this.logger = logger;

        if (name.trim().isEmpty() || (name.length() >= Stargate.getMaxTextLength())) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }

        colorDrawer = new NoLineColorFormatter();

        if (gate.getFormat() != null && gate.getFormat().isIronDoorBlockable()) {
            flags.add(PortalFlag.IRON_DOOR);
        }

        StringBuilder msg = new StringBuilder("Selected with flags ");
        for (PortalFlag flag : flags) {
            msg.append(flag.getCharacterRepresentation());
        }
        Stargate.log(Level.FINER, msg.toString());

        AbstractPortal.portalCount++;
        AbstractPortal.allUsedFlags.addAll(flags);
    }

    @Override
    public List<Location> getPortalPosition(PositionType type) {
        List<Location> positions = new ArrayList<>();
        gate.getPortalPositions().stream().filter((position) -> position.getPositionType() == type).forEach(
                (position) -> positions.add(gate.getLocation(position.getPositionLocation())));
        return positions;
    }

    @Override
    public void updateState() {
        setSignColor(null);
        if (getCurrentDestination() == null || this instanceof FixedPortal || hasFlag(PortalFlag.ALWAYS_ON)) {
            this.destination = getDestination();
        }

        Portal currentDestination = getCurrentDestination();
        if (hasFlag(PortalFlag.ALWAYS_ON) && currentDestination != null) {
            this.open(null);
        }
        if (isOpen() && currentDestination == null) {
            close(true);
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

        Stargate.addSynchronousSecAction(new DelayedAction(openDelay, () -> {
            close(openTime);
            return true;
        }));
    }

    @Override
    public void close(boolean forceClose) {
        if (!isOpen() || (hasFlag(PortalFlag.ALWAYS_ON) && !forceClose) || this.isDestroyed) {
            return;
        }
        StargateCloseEvent closeEvent = new StargateCloseEvent(this, forceClose);
        Bukkit.getPluginManager().callEvent(closeEvent);
        if (closeEvent.isCancelled()) {
            logger.logMessage(Level.FINE, "Closing event for portal " + getName() + " in network " + getNetwork().getName() + " was canceled");
            return;
        }

        logger.logMessage(Level.FINE, "Closing the portal");
        getGate().close();
        drawControlMechanisms();
        openFor = null;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        logger.logMessage(Level.FINE, String.format("isOpenForUUID = %s", (openFor == null) ? "null" : openFor.toString()));
        return ((openFor == null) || (target.getUniqueId() == openFor));
    }

    @Override
    @SuppressWarnings("unused")
    public void overrideDestination(Portal destination) {
        this.overriddenDestination = destination;
    }

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(Network targetNetwork) throws NameErrorException {
        if (targetNetwork.getPortal(this.name) != null) {
            throw new NameErrorException(null);
        }
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
            useCost = shouldCharge ? ConfigurationHelper.getInteger(ConfigurationOption.USE_COST) : 0;
        }

        Teleporter teleporter = new Teleporter(this, origin, portalFacing, entranceFace, useCost,
                Stargate.getLanguageManagerStatic().getMessage(TranslatableMessage.TELEPORT), logger);

        teleporter.teleport(target);
    }

    @Override
    public void doTeleport(Entity target) {
        Portal destination = getCurrentDestination();
        if (destination == null) {
            Teleporter teleporter = new Teleporter(this, this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.TELEPORTATION_OCCUPIED), logger);
            teleporter.teleport(target);
            return;
        }

        StargateAccessEvent accessEvent = new StargateAccessEvent(target, this, false, null);
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            logger.logMessage(Level.CONFIG, " Access event was canceled by an external plugin");
            Teleporter teleporter = new Teleporter(this, this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, accessEvent.getDenyReason(), logger);
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

        Portal destination = getDestination();
        if (destination == null) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.INVALID));
            return;
        }
        StargatePermissionManager permissionManager = new StargatePermissionManager(player);
        StargateOpenEvent stargateOpenEvent = new StargateOpenEvent(player, this, false);
        Bukkit.getPluginManager().callEvent(stargateOpenEvent);
        if (!permissionManager.hasOpenPermissions(this, destination)) {
            event.getPlayer().sendMessage(permissionManager.getDenyMessage());
            return;
        }
        if (stargateOpenEvent.isCancelled()) {
            return;
        }

        this.destination = destination;
        open(player);
        destination.open(player);
    }

    @Override
    public void setSignColor(DyeColor color) {

        /* NoLineColorFormatter should only be used during startup, this means
         * that if it has already been changed, and if there's no color to change to,
         * then the line formatter does not need to be reinstated again
         *
         * Just avoids some unnecessary minute lag
         */
        if (!(colorDrawer instanceof NoLineColorFormatter) && color == null) {
            return;
        }

        for (Location location : this.getPortalPosition(PositionType.SIGN)) {
            if (!(location.getBlock().getState() instanceof Sign)) {
                logger.logMessage(Level.WARNING, String.format("Could not find a sign for portal %s in network %s \n"
                                + "This is most likely caused from a bug // please contact developers (use ''sg about'' for github repo)",
                        this.name, this.network.getName()));
                continue;
            }
            Sign sign = (Sign) location.getBlock().getState();
            if (color == null) {
                color = sign.getColor();
            }

            if (NonLegacyMethod.CHAT_COLOR.isImplemented()) {
                colorDrawer = new LineColorFormatter(color, sign.getType());
            } else {
                colorDrawer = new LegacyLineColorFormatter();
            }
            StargateSignFormatEvent formatEvent = new StargateSignFormatEvent(this, colorDrawer, color);
            Bukkit.getPluginManager().callEvent(formatEvent);
            this.colorDrawer = formatEvent.getLineFormatter();
        }
        // Has to be done one tick later to avoid a bukkit bug
        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            this.drawControlMechanisms();
            return true;
        }));
    }

    @Override
    public Gate getGate() {
        return gate;
    }

    @Override
    public void destroy() {
        this.network.removePortal(this, true);
        this.close(true);
        String[] lines = new String[]{name, "", "", ""};
        getGate().drawControlMechanisms(lines, false);

        for (GateStructureType formatType : GateStructureType.values()) {
            for (BlockLocation loc : this.getGate().getLocations(formatType)) {
                Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                Stargate.getRegistryStatic().unRegisterLocation(formatType, loc);
            }
        }
        this.isDestroyed = true;

        Supplier<Boolean> destroyAction = () -> {
            network.updatePortals();
            return true;
        };
        Stargate.addSynchronousTickAction(new SupplierAction(destroyAction));
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

    @Override
    public String getDestinationName() {
        if (destination == null) {
            return null;
        } else {
            return destination.getName();
        }
    }

    /**
     * Gets this portal's current destination
     *
     * @return <p>This portal's current destination</p>
     */
    private Portal getCurrentDestination() {
        if (overriddenDestination == null) {
            return destination;
        } else {
            return overriddenDestination;
        }
    }

    @Override
    public String getID() {
        return NameHelper.getNormalizedName(name);
    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {
        if ((this.activator != null && !event.getPlayer().getUniqueId().equals(this.activator))) {
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            this.drawControlMechanisms();
            return;
        }
        PermissionManager permissionManager = new StargatePermissionManager(event.getPlayer());
        StargateAccessEvent accessEvent = new StargateAccessEvent(event.getPlayer(), this, !permissionManager.hasAccessPermission(this),
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            event.getPlayer().sendMessage(accessEvent.getDenyReason());
            return;
        }
        String[] signText = {
                this.colorDrawer.formatLine(Stargate.getLanguageManagerStatic().getString(TranslatableMessage.PREFIX).trim()),
                this.colorDrawer
                        .formatLine(Stargate.getLanguageManagerStatic().getString(TranslatableMessage.GATE_OWNED_BY)),
                this.colorDrawer.formatLine(Bukkit.getOfflinePlayer(ownerUUID).getName()),
                this.colorDrawer.formatLine(getAllFlagsString().replaceAll("[0-9]", ""))};

        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            gate.drawControlMechanisms(signText, false);
            return true;
        }));
        activate(event.getPlayer());
    }


    /**
     * Activates this portal for the given player
     *
     * @param player <p>The player to activate this portal for</p>
     */
    protected void activate(Player player) {
        this.activator = player.getUniqueId();
        long activationTime = System.currentTimeMillis();
        this.activatedTime = activationTime;

        //Schedule for deactivation
        Stargate.addSynchronousSecAction(new DelayedAction(ACTIVE_DELAY, () -> {
            deactivate(activationTime);
            return true;
        }));
    }


    /**
     * De-activates this portal if necessary
     *
     * <p>The activated time must match to make sure to skip de-activation requests except for the one cancelling the
     * newest portal activation.</p>
     *
     * @param activatedTime <p>The time this portal was activated</p>
     */
    protected void deactivate(long activatedTime) {
        if (activatedTime != this.activatedTime) {
            return;
        }
        deactivate();
    }

    /**
     * De-activates this portal
     */
    protected void deactivate() {
        if (this.isDestroyed) {
            return;
        }

        //Call the deactivate event to notify add-ons
        StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        this.activator = null;
        drawControlMechanisms();
    }
    
    @Override
    public void setMetaData(String data) {
        try {
            Stargate.getStorageAPIStatic().setPortalMetaData(this, data, getPortalType());
        } catch (StorageWriteException e) {
            e.printStackTrace();
        }
    };
    
    @Override
    public String getMetaData() {
        try {
            return Stargate.getStorageAPIStatic().getPortalMetaData(this,getPortalType());
        } catch (StorageReadException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public PortalType getPortalType() {
        return (flags.contains(PortalFlag.FANCY_INTER_SERVER) ? PortalType.INTER_SERVER : PortalType.LOCAL);
    }
}