package org.sgrewritten.stargate.network.portal;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateAccessPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateClosePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateDeactivatePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateSignDyeChangePortalEvent;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.behavior.PortalBehavior;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.colors.ColorRegistry;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.formatting.LegacyLineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.NoLineColorFormatter;
import org.sgrewritten.stargate.property.NonLegacyClass;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * An abstract implementation of a real portal
 *
 * @author Thorin
 */
public class StargatePortal implements RealPortal {

    /**
     * Used in bStats metrics
     */
    public static int portalCount = 0;
    /**
     * Used for bStats metrics, this is every flag that has been used by all portals
     */
    public static final Set<PortalFlag> allUsedFlags = new HashSet<>();

    private static final int OPEN_DELAY = 20 * 20; // ticks
    protected Network network;
    protected String name;
    protected UUID openFor;
    protected Portal overriddenDestination = null;
    private long openTime = -1;
    private UUID ownerUUID;
    private final GateAPI gate;
    private final Set<PortalFlag> flags;
    protected long activatedTime;
    protected UUID activator;
    protected boolean isDestroyed = false;
    protected final LanguageManager languageManager;
    private final StargateEconomyAPI economyManager;
    private static final int ACTIVE_DELAY = 15 * 20; // ticks
    private @Nullable String metaData;
    private boolean savedToStorage = false;
    private PortalBehavior behavior;
    private boolean active = false;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameLengthException <p>If the portal name is invalid</p>
     */
    public StargatePortal(Network network, String name, Set<PortalFlag> flags, GateAPI gate,
                          UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyManager, @Nullable String metaData)
            throws NameLengthException {
        this.ownerUUID = Objects.requireNonNull(ownerUUID);
        this.network = Objects.requireNonNull(network);
        this.name = Objects.requireNonNull(name);
        this.flags = Objects.requireNonNull(flags);
        this.gate = Objects.requireNonNull(gate);
        this.languageManager = Objects.requireNonNull(languageManager);
        this.economyManager = Objects.requireNonNull(economyManager);
        this.metaData = metaData;

        name = NameHelper.getTrimmedName(name);
        if (NameHelper.isInvalidName(name)) {
            throw new NameLengthException("Invalid length of name '" + name + "' , name length must be above 0 and under " + StargateConstant.MAX_TEXT_LENGTH);
        }
        gate.getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.SIGN)
                .forEach(portalPosition -> portalPosition.setAttachment(new NoLineColorFormatter()));

        if (gate.getFormat().isIronDoorBlockable()) {
            flags.add(StargateFlag.IRON_DOOR);
        }
        gate.getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.BUTTON)
                .forEach(portalPosition -> gate.redrawPosition(portalPosition, null));
        StringBuilder msg = new StringBuilder("Selected with flags ");
        for (PortalFlag flag : flags) {
            msg.append(flag.getCharacterRepresentation());
        }
        Stargate.log(Level.FINER, msg.toString());

        StargatePortal.portalCount++;
        StargatePortal.allUsedFlags.addAll(flags);
    }

    @Override
    public GlobalPortalId getGlobalId() {
        return GlobalPortalId.getFromPortal(this);
    }

    @Override
    public List<Location> getPortalPosition(PositionType type) {
        List<Location> positions = new ArrayList<>();
        gate.getPortalPositions().stream().filter(position -> position.getPositionType() == type).forEach(
                position -> positions.add(gate.getLocation(position.getRelativePositionLocation())));
        return positions;
    }

    @Override
    public void updateState() {
        gate.getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.SIGN)
                .forEach(portalPosition -> this.setSignColor(null, portalPosition));
        this.behavior.update();
        Portal currentDestination = getCurrentDestination(this.behavior.getDestination());
        if (hasFlag(StargateFlag.ALWAYS_ON) && currentDestination != null) {
            this.open(currentDestination, null);
        }
        if (isOpen() && currentDestination == null) {
            close(true);
        }
        this.redrawSigns();
    }

    @Override
    public boolean isOpen() {
        return getGate().isOpen();
    }

    @Override
    public void open(@Nullable Portal destination, @Nullable Player actor) {
        if (hasFlag(StargateFlag.ALWAYS_ON) && getCurrentDestination(destination) == null) {
            return;
        }
        getGate().open();
        if (actor != null) {
            this.openFor = actor.getUniqueId();
        }
        if (hasFlag(StargateFlag.ALWAYS_ON)) {
            return;
        }
        final long openTimeForAction = System.currentTimeMillis();
        this.openTime = openTimeForAction;

        new StargateGlobalTask() {
            @Override
            public void run() {
                close(openTimeForAction);
            }
        }.runDelayed(OPEN_DELAY);
        if (destination != null) {
            destination.open(actor);
        }
    }

    @Override
    public void open(Player actor) {
        this.open(null, actor);
    }

    @Override
    public void close(boolean forceClose) {
        if (!isOpen() || (hasFlag(StargateFlag.ALWAYS_ON) && !forceClose) || this.isDestroyed) {
            return;
        }
        StargateClosePortalEvent closeEvent = new StargateClosePortalEvent(this, forceClose);
        Bukkit.getPluginManager().callEvent(closeEvent);
        if (closeEvent.isCancelled()) {
            Stargate.log(Level.FINE, "Closing event for portal " + getName() + " in network " + getNetwork().getName() + " was canceled");
            return;
        }

        Stargate.log(Level.FINE, "Closing the portal");
        getGate().close();
        redrawSigns();
        openFor = null;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        Stargate.log(Level.FINE, String.format("isOpenForUUID = %s", (openFor == null) ? "null" : openFor.toString()));
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
    public void setNetwork(Network targetNetwork) throws NameConflictException {
        if (targetNetwork.getPortal(this.name) != null) {
            throw new NameConflictException(String.format("Portal of name %s already exists in network %s", this.name, targetNetwork.getId()), false);
        }
        this.network = targetNetwork;
        updateState();
    }

    @Override
    public void setOwner(UUID targetPlayer) {
        this.ownerUUID = targetPlayer;
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {

        BlockFace portalFacing = gate.getFacing().getOppositeFace();
        if (flags.contains(StargateFlag.BACKWARDS)) {
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

            boolean shouldCharge = !(this.hasFlag(StargateFlag.FREE) || origin.hasFlag(StargateFlag.FREE))
                    && target instanceof Player && !target.hasPermission(BypassPermission.COST_USE.getPermissionString());
            useCost = shouldCharge ? ConfigurationHelper.getInteger(ConfigurationOption.USE_COST) : 0;
        }

        Teleporter teleporter = new Teleporter(this, origin, portalFacing, entranceFace, useCost,
                languageManager.getMessage(TranslatableMessage.TELEPORT), languageManager, economyManager);

        teleporter.teleport(target);
    }

    @Override
    public void doTeleport(@NotNull Entity target, @Nullable Portal destination) {
        if (destination == null) {
            Teleporter teleporter = new Teleporter(this, this, gate.getFacing().getOppositeFace(), gate.getFacing(),
                    0, languageManager.getErrorMessage(TranslatableMessage.TELEPORTATION_OCCUPIED), languageManager, economyManager);
            teleporter.teleport(target);
            return;
        }

        StargateAccessPortalEvent accessEvent = new StargateAccessPortalEvent(target, this, false, null);
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
    public void doTeleport(@NotNull Entity target) {
        if(overriddenDestination != null){
            this.doTeleport(target, overriddenDestination);
            return;
        }
        this.doTeleport(target, behavior.getDestination());
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
    public void addFlag(PortalFlag flag) throws UnsupportedOperationException {
        if (flag.isBehaviorFlag()) {
            throw new UnsupportedOperationException("Adding selector type flags is not currently implemented");
        }
        if (NetworkType.isNetworkTypeFlag(flag)) {
            throw new UnsupportedOperationException("Network deciding flags change is not currently implemented");
        }
        this.flags.add(flag);
    }

    @Override
    public void removeFlag(PortalFlag flag) throws UnsupportedOperationException {
        if (flag.isBehaviorFlag()) {
            throw new UnsupportedOperationException("Removing selector type flags is not currently implemented");
        }
        if (NetworkType.isNetworkTypeFlag(flag)) {
            throw new UnsupportedOperationException("Network deciding flags change is not currently implemented");
        }
        flags.remove(flag);
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
    public void setSignColor(@Nullable DyeColor changedColor, @NotNull PortalPosition portalPosition) {
        if (!(portalPosition.getAttachment() instanceof LineFormatter lineFormatter)) {
            throw new IllegalArgumentException("Could not find line formatter");
        }
        /* NoLineColorFormatter should only be used during startup, this means
         * that if it has already been changed, and if there's no color to change to,
         * then the line formatter does not need to be reinstated again
         *
         * Just avoids some unnecessary minute lag
         */
        if (!(lineFormatter instanceof NoLineColorFormatter) && changedColor == null) {
            return;
        }
        Location positionLocation = gate.getLocation(portalPosition.getRelativePositionLocation());
        new StargateRegionTask(positionLocation) {
            @Override
            public void run() {
                updateColorDrawer(positionLocation, changedColor, portalPosition);
                Block signBlock = positionLocation.getBlock();
                if (signBlock.getState() instanceof Sign sign) {
                    sign.setColor(ColorRegistry.DEFAULT_DYE_COLOR);
                    sign.update();
                }
            }
        }.runNow();
        new StargateGlobalTask() {
            @Override
            public void run() {
                LineData[] lineData = behavior.getLines();
                getGate().redrawPosition(portalPosition, lineData);
            }
        }.runDelayed(2);
    }

    private void updateColorDrawer(Location location, DyeColor changedColor, PortalPosition portalPosition) {
        if (!(location.getBlock().getState() instanceof Sign sign)) {
            Stargate.log(Level.WARNING, String.format("Could not find a sign for portal %s in network %s %n"
                            + "This is most likely caused from a bug // please contact developers (use ''sg about'' for github repo)",
                    this.name, this.network.getName()));
            return;
        }
        DyeColor color;
        if (changedColor == null) {
            color = sign.getColor();
        } else {
            StargateSignDyeChangePortalEvent event = new StargateSignDyeChangePortalEvent(this, changedColor, location, portalPosition);
            Bukkit.getPluginManager().callEvent(event);
            color = changedColor;
        }
        LineFormatter lineFormatter;
        if (NonLegacyClass.CHAT_COLOR.isImplemented()) {
            lineFormatter = new LineColorFormatter(color, sign.getType());
        } else {
            lineFormatter = new LegacyLineColorFormatter();
        }
        portalPosition.setAttachment(lineFormatter);
    }

    @Override
    public GateAPI getGate() {
        return gate;
    }

    @Override
    public void destroy() {
        this.isDestroyed = true;
        behavior.onDestroy();
        this.close(true);
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

    private Portal getCurrentDestination(Portal destination) {
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
    public void activate(@Nullable Player player) {
        if (player != null) {
            this.activator = player.getUniqueId();
        } else {
            this.activator = null;
        }
        long activationTime = System.currentTimeMillis();
        this.activatedTime = activationTime;
        this.active = true;

        //Schedule for deactivation
        new StargateGlobalTask() {
            @Override
            public void run() {
                deactivate(activationTime);
            }
        }.runDelayed(ACTIVE_DELAY);
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public UUID getActivatorUUID() {
        return activator;
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

    @Override
    public void deactivate() {
        if (this.isDestroyed) {
            return;
        }
        //Call the deactivate event to notify add-ons
        StargateDeactivatePortalEvent event = new StargateDeactivatePortalEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        this.activator = null;
        this.active = false;
        redrawSigns();
    }

    @Override
    public void setMetadata(String data) {
        try {
            this.metaData = data;
            if (this.savedToStorage) {
                Stargate.getStorageAPIStatic().setPortalMetaData(this, data, getStorageType());
            }
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public String getMetadata() {
        if (this.metaData != null || !this.savedToStorage) {
            return this.metaData;
        }
        try {
            this.metaData = Stargate.getStorageAPIStatic().getPortalMetaData(this, getStorageType());
            return this.metaData;
        } catch (StorageReadException e) {
            Stargate.log(e);
            return null;
        }
    }

    @Override
    public StorageType getStorageType() {
        return (flags.contains(StargateFlag.INTERSERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL);
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    public BlockFace getExitFacing() {
        return flags.contains(StargateFlag.BACKWARDS) ? getGate().getFacing() : getGate().getFacing().getOppositeFace();
    }

    public void setSavedToStorage() {
        this.savedToStorage = true;
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    @Override
    public PortalBehavior getBehavior() {
        return this.behavior;
    }

    @Override
    public void setBehavior(PortalBehavior portalBehavior) {
        clearBehaviorFlags();
        flags.add(portalBehavior.getAttachedFlag());
        this.behavior = Objects.requireNonNull(portalBehavior);
        this.behavior.assignPortal(this);
        Portal destination = this.behavior.getDestination();
        if (hasFlag(StargateFlag.ALWAYS_ON) && destination != null) {
            open(destination, null);
        }
    }

    @Override
    public void redrawSigns() {
        LineData[] lineData = behavior.getLines();
        getGate().getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.SIGN)
                .forEach(portalPosition -> getGate().redrawPosition(portalPosition, lineData));
    }

    private void clearBehaviorFlags() {
        List<PortalFlag> flagsToRemove = flags.stream().filter(flag -> flag.isBehaviorFlag() && !flag.isCustom()).toList();
        flagsToRemove.forEach(flags::remove);
    }
}