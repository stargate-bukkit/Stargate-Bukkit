package org.sgrewritten.stargate.network.portal;

import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateAccessPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateClosePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateDeactivatePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateOpenPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateSignDyeChangePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.format.SignLine;
import org.sgrewritten.stargate.api.network.portal.format.SignLineType;
import org.sgrewritten.stargate.api.network.portal.format.TextLine;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.colors.ColorRegistry;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.formatting.LegacyLineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.network.portal.formatting.NoLineColorFormatter;
import org.sgrewritten.stargate.property.NonLegacyClass;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.MessageUtils;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.portal.PortalHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

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

    private static final int OPEN_DELAY = 20 * 20; // ticks
    protected Network network;
    protected String name;
    protected UUID openFor;
    protected Portal destination = null;
    protected Portal overriddenDestination = null;
    protected LineFormatter colorDrawer;

    private long openTime = -1;
    private UUID ownerUUID;
    private final GateAPI gate;
    private final Set<PortalFlag> flags;
    private final Set<Character> unrecognisedFlags;
    protected long activatedTime;
    protected UUID activator;
    protected boolean isDestroyed = false;
    protected final LanguageManager languageManager;
    private final StargateEconomyAPI economyManager;
    private static final int ACTIVE_DELAY = 15 * 20; // ticks
    private static final Pattern INTERNAL_FLAG = Pattern.compile("\\d");
    private @Nullable String metaData;
    private boolean savedToStorage = false;

    /**
     * Instantiates a new abstract portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameLengthException <p>If the portal name is invalid</p>
     */
    AbstractPortal(Network network, String name, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, GateAPI gate,
                   UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyManager, @Nullable String metaData)
            throws NameLengthException {
        this.ownerUUID = Objects.requireNonNull(ownerUUID);
        this.network = Objects.requireNonNull(network);
        this.name = Objects.requireNonNull(name);
        this.flags = Objects.requireNonNull(flags);
        this.unrecognisedFlags = Objects.requireNonNull(unrecognisedFlags);
        this.gate = Objects.requireNonNull(gate);
        this.languageManager = Objects.requireNonNull(languageManager);
        this.economyManager = Objects.requireNonNull(economyManager);
        this.metaData = metaData;

        name = NameHelper.getTrimmedName(name);
        if (NameHelper.isInvalidName(name)) {
            throw new NameLengthException("Invalid length of name '" + name + "' , name length must be above 0 and under " + StargateConstant.MAX_TEXT_LENGTH);
        }

        colorDrawer = new NoLineColorFormatter();

        if (gate.getFormat().isIronDoorBlockable()) {
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
    public List<Location> getPortalPosition(PositionType type) {
        List<Location> positions = new ArrayList<>();
        gate.getPortalPositions().stream().filter(position -> position.getPositionType() == type).forEach(
                position -> positions.add(gate.getLocation(position.getRelativePositionLocation())));
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
        SignLine[] lines = getDrawnControlLines();
        this.getGate().drawControlMechanisms(lines, !this.hasFlag(PortalFlag.ALWAYS_ON));
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
        final long openTimeForAction = System.currentTimeMillis();
        this.openTime = openTimeForAction;

        new StargateGlobalTask(() -> close(openTimeForAction)).runDelayed(OPEN_DELAY);
    }

    @Override
    public void close(boolean forceClose) {
        if (!isOpen() || (hasFlag(PortalFlag.ALWAYS_ON) && !forceClose) || this.isDestroyed) {
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
        SignLine[] lines = getDrawnControlLines();
        gate.drawControlMechanisms(lines, this.hasFlag(PortalFlag.ALWAYS_ON));
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
        //TODO: update network in database
        this.getDrawnControlLines();
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
        Portal currentDestination = getCurrentDestination();
        if (currentDestination == null) {
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

        currentDestination.teleportHere(target, this);
        currentDestination.close(false);
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
    public boolean hasFlag(char flag) {
        return unrecognisedFlags.contains(Character.toUpperCase(flag)) || (ExceptionHelper.doesNotThrow(() -> PortalFlag.valueOf(flag)) && flags.contains(PortalFlag.valueOf(flag)));
    }

    @Override
    public void addFlag(Character flag) throws UnsupportedOperationException {
        try {
            PortalFlag portalFlag = PortalFlag.valueOf(flag);
            if (portalFlag.isSelectorTypeFlag()) {
                throw new UnsupportedOperationException("Adding selector type flags is not currently implemented");
            }
            if (NetworkType.isNetworkTypeFlag(portalFlag)) {
                throw new UnsupportedOperationException("Network deciding flags change is not currently implemented");
            }
            this.flags.add(portalFlag);
        } catch (IllegalArgumentException e) {
            unrecognisedFlags.add(flag);
        }
    }

    @Override
    public void removeFlag(Character flag) throws UnsupportedOperationException {
        try {
            PortalFlag portalFlag = PortalFlag.valueOf(flag);
            if (portalFlag.isSelectorTypeFlag()) {
                throw new UnsupportedOperationException("Removing selector type flags is not currently implemented");
            }
            if (NetworkType.isNetworkTypeFlag(portalFlag)) {
                throw new UnsupportedOperationException("Network deciding flags change is not currently implemented");
            }
            flags.remove(portalFlag);
        } catch (IllegalArgumentException e) {
            unrecognisedFlags.remove(flag);
        }
    }

    @Override
    public String getAllFlagsString() {
        StringBuilder builder = new StringBuilder();
        for (Character flagChar : this.unrecognisedFlags) {
            builder.append(flagChar);
        }
        return PortalHelper.flagsToString(flags) + builder;
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

        Portal selectedDestination = getDestination();
        String message = null;
        MessageType messageType = null;
        boolean cancelled = false;

        if (selectedDestination == null) {
            message = languageManager.getErrorMessage(TranslatableMessage.INVALID);
            messageType = MessageType.DESTINATION_EMPTY;
            cancelled = true;
        }
        StargatePermissionManager permissionManager = new StargatePermissionManager(player, languageManager);
        if (!permissionManager.hasOpenPermissions(this, selectedDestination)) {
            message = permissionManager.getDenyMessage();
            messageType = MessageType.DENY;
            cancelled = true;
        }
        StargateOpenPortalEvent stargateOpenEvent = new StargateOpenPortalEvent(player, this, selectedDestination, cancelled, false);
        Bukkit.getPluginManager().callEvent(stargateOpenEvent);

        if (stargateOpenEvent.isCancelled()) {
            if (message != null) {
                MessageUtils.sendMessageFromPortal(this, player, message, messageType);
            }
            return;
        }

        this.destination = stargateOpenEvent.getDestination();
        open(player);
        if (this.destination != null) {
            this.destination.open(player);
        }
    }

    @Override
    public void setSignColor(@Nullable DyeColor changedColor) {
        /* NoLineColorFormatter should only be used during startup, this means
         * that if it has already been changed, and if there's no color to change to,
         * then the line formatter does not need to be reinstated again
         *
         * Just avoids some unnecessary minute lag
         */
        if (!(colorDrawer instanceof NoLineColorFormatter) && changedColor == null) {
            return;
        }
        for (PortalPosition portalPosition : gate.getPortalPositions()) {
            if (portalPosition.getPositionType() != PositionType.SIGN) {
                continue;
            }
            Location location = gate.getLocation(portalPosition.getRelativePositionLocation());
            new StargateRegionTask(location, () ->
                    updateColorDrawer(location, changedColor, portalPosition)
            ).run();
        }
        // Has to be done one tick later to avoid a bukkit bug
        if (changedColor == null) {
            gate.getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.SIGN).forEach(portalPosition -> {
                final Block signBlock = gate.getLocation(portalPosition.getRelativePositionLocation()).getBlock();
                new StargateRegionTask(signBlock.getLocation(), () -> {
                    if (Tag.WALL_SIGNS.isTagged(signBlock.getType())) {
                        Sign sign = (Sign) signBlock.getState();
                        sign.setColor(ColorRegistry.DEFAULT_DYE_COLOR);
                        sign.update();
                    }
                }).run();
            });
        }

        new StargateGlobalTask(() -> {
            SignLine[] lines = this.getDrawnControlLines();
            getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
        }).runDelayed(2);
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

        if (NonLegacyClass.CHAT_COLOR.isImplemented()) {
            colorDrawer = new LineColorFormatter(color, sign.getType());
        } else {
            colorDrawer = new LegacyLineColorFormatter();
        }
    }

    @Override
    public GateAPI getGate() {
        return gate;
    }

    @Override
    public void destroy() {
        this.isDestroyed = true;
        // drawing the sign first is necessary, as the portal positions gets unregistered from the gate later on
        SignLine[] lines = new SignLine[]{new TextLine(getName(), SignLineType.TEXT), new TextLine(), new TextLine(), new TextLine()};
        getGate().drawControlMechanisms(lines, false);
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
    public String getId() {
        return NameHelper.getNormalizedName(name);
    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {
        if ((this.activator != null && !event.getPlayer().getUniqueId().equals(this.activator))) {
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            this.getDrawnControlLines();
            return;
        }
        PermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), languageManager);
        StargateAccessPortalEvent accessEvent = new StargateAccessPortalEvent(event.getPlayer(), this, !permissionManager.hasAccessPermission(this),
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            MessageUtils.sendMessageFromPortal(this, event.getPlayer(), accessEvent.getDenyReason(), MessageType.DENY);
            return;
        }

        SignLine[] lines = new SignLine[]{
                new TextLine(this.colorDrawer.formatLine(languageManager.getString(TranslatableMessage.PREFIX).trim())),
                new TextLine(this.colorDrawer
                        .formatLine(languageManager.getString(TranslatableMessage.GATE_OWNED_BY))),
                new TextLine(this.colorDrawer.formatLine(Bukkit.getOfflinePlayer(ownerUUID).getName())),
                new TextLine(this.colorDrawer.formatLine(INTERNAL_FLAG.matcher(getAllFlagsString()).replaceAll("")))
        };
        new StargateGlobalTask(() -> gate.drawControlMechanisms(lines, false)).run();
        activate(event.getPlayer());
    }

    @Override
    public void activate(Player player) {
        this.activator = player.getUniqueId();
        long activationTime = System.currentTimeMillis();
        this.activatedTime = activationTime;

        //Schedule for deactivation
        new StargateGlobalTask(() -> deactivate(activationTime)).runDelayed(ACTIVE_DELAY);
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
        StargateDeactivatePortalEvent event = new StargateDeactivatePortalEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        this.activator = null;
        SignLine[] lines = getDrawnControlLines();
        gate.drawControlMechanisms(lines, !flags.contains(PortalFlag.ALWAYS_ON));
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
        return (flags.contains(PortalFlag.FANCY_INTER_SERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL);
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    public BlockFace getExitFacing() {
        return flags.contains(PortalFlag.BACKWARDS) ? getGate().getFacing() : getGate().getFacing().getOppositeFace();
    }

    public void setSavedToStorage() {
        this.savedToStorage = true;
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

}