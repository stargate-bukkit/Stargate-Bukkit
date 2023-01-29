package org.sgrewritten.stargate.network.portal;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.DelayedAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.StargateAccessEvent;
import org.sgrewritten.stargate.api.event.StargateCloseEvent;
import org.sgrewritten.stargate.api.event.StargateDeactivateEvent;
import org.sgrewritten.stargate.api.event.StargateOpenEvent;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.manager.PermissionManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.FormattableObject;
import org.sgrewritten.stargate.api.network.portal.formatting.StringFormattableObject;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.util.HashSet;
import java.util.Objects;
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
    public static final Set<PortalFlag> allUsedFlags = new HashSet<>();

    protected final int openDelay = 20;
    protected Network network;
    protected String name;
    protected UUID openFor;
    protected Portal destination = null;
    protected Portal overriddenDestination = null;

    private long openTime = -1;
    private UUID ownerUUID;
    private final Gate gate;
    private final Set<PortalFlag> flags;

    protected long activatedTime;
    protected UUID activator;
    protected boolean isDestroyed = false;
    protected final LanguageManager languageManager;
    private final StargateEconomyAPI economyManager;
    private static final int ACTIVE_DELAY = 15;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameLengthException <p>If the portal name is invalid</p>
     */
    AbstractPortal(Network network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyManager)
            throws NameLengthException {
        this.ownerUUID = Objects.requireNonNull(ownerUUID);
        this.network = Objects.requireNonNull(network);
        this.name = Objects.requireNonNull(name);
        this.flags = Objects.requireNonNull(flags);
        this.gate = Objects.requireNonNull(gate);
        this.languageManager = Objects.requireNonNull(languageManager);
        this.economyManager = Objects.requireNonNull(economyManager);

        name = NameHelper.getTrimmedName(name);
        if (NameHelper.isInvalidName(name)) {
            throw new NameLengthException("Invalid length of name '" + name + "' , namelength must be above 0 and under " + Stargate.getMaxTextLength());
        }

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
    public GlobalPortalId getGlobalId() {
        return GlobalPortalId.getFromPortal(this);
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
    public void open(@Nullable Player actor) {
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
            Stargate.log(Level.FINE, "Closing event for portal " + getName() + " in network " + getNetwork().getName() + " was canceled");
            return;
        }

        Stargate.log(Level.FINE, "Closing the portal");
        getGate().close();
        drawControlMechanisms();
        openFor = null;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        Stargate.log(Level.FINE, String.format("isOpenForUUID = %s", (openFor == null) ? "null" : openFor.toString()));
        return ((openFor == null) || (target.getUniqueId() == openFor));
    }

    @Override
    @SuppressWarnings("unused")
    public void overrideDestination(@Nullable Portal destination) {
        this.overriddenDestination = destination;
    }

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(Network targetNetwork) throws NameConflictException {
        if (targetNetwork.getPortal(this.name) != null) {
            throw new NameConflictException(String.format("Portal of name %s already exists in network %s", this.name, targetNetwork.getId()), false);
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
                languageManager.getMessage(TranslatableMessage.TELEPORT), languageManager, economyManager);

        teleporter.teleport(target);
    }

    @Override
    public void doTeleport(Entity target) {
        Portal destination = getCurrentDestination();
        if (destination == null) {
            Teleporter teleporter = new Teleporter(this, this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, languageManager.getErrorMessage(TranslatableMessage.TELEPORTATION_OCCUPIED), languageManager, economyManager);
            teleporter.teleport(target);
            return;
        }

        StargateAccessEvent accessEvent = new StargateAccessEvent(target, this, false, null);
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            Stargate.log(Level.CONFIG, " Access event was canceled by an external plugin");
            Teleporter teleporter = new Teleporter(this, this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, accessEvent.getDenyReason(), languageManager, economyManager);
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
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.INVALID));
            return;
        }
        StargatePermissionManager permissionManager = new StargatePermissionManager(player, languageManager);
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
        GateTextDisplayHandler display = this.getPortalTextDisplay();
        if (display == null) {
            return;
        }
        display.setTextColor(color, this);
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
        GateTextDisplayHandler display = this.getPortalTextDisplay();
        if (display != null) {
            display.displayText(new FormattableObject[]{new StringFormattableObject(name), new StringFormattableObject(""), new StringFormattableObject(""), new StringFormattableObject("")});
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
        Portal destination = getCurrentDestination();
        if (destination == null) {
            return null;
        } else {
            return destination.getName();
        }
    }

    @Override
    public Portal getCurrentDestination() {
        if (overriddenDestination == null) {
            return destination;
        } else {
            return overriddenDestination;
        }
    }

    @Override
    public String getId() {
        return NameHelper.getNormalizedName(name);
    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {
        if ((this.activator != null && !event.getPlayer().getUniqueId().equals(this.activator))) {
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            //Reset the information display to normal if player is not sneaking
            this.drawControlMechanisms();
            return;
        }
        GateTextDisplayHandler displayHandler = this.getPortalTextDisplay();
        if (displayHandler == null) {
            return;
        }
        PermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), languageManager);
        StargateAccessEvent accessEvent = new StargateAccessEvent(event.getPlayer(), this, !permissionManager.hasAccessPermission(this),
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            event.getPlayer().sendMessage(accessEvent.getDenyReason());
            return;
        }
        String nullablePlayerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
        String playerName = nullablePlayerName == null ? "null" : nullablePlayerName;
        FormattableObject[] signText = {
                new StringFormattableObject(languageManager.getString(TranslatableMessage.PREFIX).trim()),
                new StringFormattableObject(languageManager.getString(TranslatableMessage.GATE_OWNED_BY)),
                new StringFormattableObject(playerName),
                new StringFormattableObject(getAllFlagsString().replaceAll("[0-9]", ""))};
        displayHandler.displayText(signText);
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
            Stargate.getStorageAPIStatic().setPortalMetaData(this, data, getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public String getMetaData() {
        try {
            return Stargate.getStorageAPIStatic().getPortalMetaData(this, getStorageType());
        } catch (StorageReadException e) {
            Stargate.log(e);
            return null;
        }
    }

    @Override
    public StorageType getStorageType() {
        return (flags.contains(PortalFlag.FANCY_INTER_SERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL);
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    public BlockFace getExitFacing() {
        return flags.contains(PortalFlag.BACKWARDS) ? getGate().getFacing() : getGate().getFacing().getOppositeFace();
    }

    protected @Nullable GateTextDisplayHandler getPortalTextDisplay() {
        ControlMechanism mechanism = this.getGate().getPortalControlMechanism(MechanismType.SIGN);
        if (mechanism == null) {
            return null;
        }
        return (GateTextDisplayHandler) mechanism;
    }

}