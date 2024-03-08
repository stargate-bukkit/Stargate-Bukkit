package org.sgrewritten.stargate.api.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateCreatePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.ExplicitGateBuilder;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateBuilder;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.colors.ColorRegistry;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.LocalisedMessageException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.manager.UnrestricedPermissionManager;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.util.EconomyHelper;
import org.sgrewritten.stargate.util.MessageUtils;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.util.SpawnDetectionHelper;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;
import org.sgrewritten.stargate.util.VectorUtils;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Used to build portals
 */
public class PortalBuilder {

    private final StargateAPI stargateAPI;
    private String networkString = "";
    private @Nullable GateAPI gateAPI;
    private final OfflinePlayer owner;
    private String flagsString;
    private final String portalName;
    private @Nullable Network network;
    private @Nullable GateBuilder gateBuilder;
    private @Nullable PermissionManager permissionManager;
    private double cost = 0;
    private @Nullable String destinationName;
    private @Nullable String serverName;
    private @Nullable Player eventTarget;
    private @Nullable Entity messageTarget;
    private @Nullable Player economyTarget;
    private boolean adaptiveGatePositionGeneration = false;
    private String metaData;
    private Set<PortalFlag> flags;
    private Player permissionTarget;

    /**
     * Construct an instance of a PortalBuilder
     *
     * @param stargateAPI <p>The stargate api</p>
     * @param owner       <p>The owner of the portal</p>
     * @param portalName  <p>The name of the portal</p>
     */
    public PortalBuilder(StargateAPI stargateAPI, OfflinePlayer owner, String portalName) {
        this.stargateAPI = Objects.requireNonNull(stargateAPI);
        this.portalName = Objects.requireNonNull(portalName);
        this.owner = Objects.requireNonNull(owner);
    }

    /**
     * Check for all stargate related permissions for target
     *
     * @param permissionTarget <p>The target of the permissions</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder addPermissionCheck(@NotNull Player permissionTarget) {
        Objects.requireNonNull(permissionTarget);
        this.permissionManager = new StargatePermissionManager(permissionTarget, stargateAPI.getLanguageManager());
        this.permissionTarget = permissionTarget;
        return this;
    }

    /**
     * Set a cost for this portal
     *
     * @param cost          <p>A cost</p>
     * @param economyTarget <p>The payee</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setCost(double cost, @NotNull Player economyTarget) {
        this.cost = cost;
        this.economyTarget = Objects.requireNonNull(economyTarget);
        return this;
    }

    /**
     * Make the builder throw an {@link StargateCreatePortalEvent}
     *
     * @param eventTarget <p>The target of the event</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder addEventHandling(@Nullable Player eventTarget) {
        this.eventTarget = eventTarget;
        return this;
    }

    /**
     * @param messageTarget <p>The entity that receives any messages related to the attempt of the portal creation</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder addMessageReceiver(@Nullable Entity messageTarget) {
        this.messageTarget = messageTarget;
        return this;
    }

    /**
     * @param destinationName <p>Set a fixed destination portal</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setDestination(@Nullable String destinationName) {
        this.destinationName = destinationName;
        return this;
    }

    /**
     * Requires the constructor {@link PortalBuilder#PortalBuilder(StargateAPI, OfflinePlayer, String)} to matter
     *
     * @param adaptiveGatePositionGeneration <p>Whether to generate portal positions for the gate</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setAdaptiveGatePositionGeneration(boolean adaptiveGatePositionGeneration) {
        this.adaptiveGatePositionGeneration = adaptiveGatePositionGeneration;
        return this;
    }

    /**
     * @param metaData <p>The metadata for the portal</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setMetaData(String metaData) {
        this.metaData = metaData;
        return this;
    }

    /**
     * Set the name of the server this portal should point to (only relevant for bungee portals)
     *
     * @param serverName <p>The name of the server this portal points to</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setDestinationServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * @param network <p>the network this portal points to</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setNetwork(@NotNull Network network) {
        this.network = Objects.requireNonNull(network);
        this.networkString = null;
        return this;
    }

    /**
     * @param networkName <p>The network argument to select networks from</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setNetwork(@NotNull String networkName) {
        this.network = null;
        this.networkString = Objects.requireNonNull(networkName);
        return this;
    }

    /**
     * @param gateAPI <p>The gate to be used by this portal builder</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setGate(GateAPI gateAPI) {
        this.gateAPI = Objects.requireNonNull(gateAPI);
        this.gateBuilder = null;
        return this;
    }

    /**
     * Set the way the gate should be constructed, see {@link ExplicitGateBuilder} for more information
     *
     * @param topLeft       <p>Top left location</p>
     * @param gateFormatAPI <p>The gate format to be used when making this gate</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setGateBuilder(@NotNull Location topLeft, @NotNull GateFormatAPI gateFormatAPI) {
        this.gateBuilder = new ExplicitGateBuilder(stargateAPI.getRegistry(), topLeft, gateFormatAPI);
        this.gateAPI = null;
        return this;
    }

    /**
     * Set the way the gate should be constructed, see {@link ExplicitGateBuilder} for more information
     *
     * @param topLeft        <p>Top left location</p>
     * @param gateFormatName <p>The name of the gate format</p>
     * @return <p>This portal builder</p>
     * @throws IllegalArgumentException <p>If the gate format has not been registered</p>
     */
    public PortalBuilder setGateBuilder(@NotNull Location topLeft, @NotNull String gateFormatName) {
        GateFormatAPI gateFormatAPI = GateFormatRegistry.getFormat(gateFormatName);
        if (gateFormatAPI == null) {
            throw new IllegalArgumentException("No such gate format: " + gateFormatName);
        }
        this.gateBuilder = new ExplicitGateBuilder(stargateAPI.getRegistry(), topLeft, gateFormatAPI);
        this.gateAPI = null;
        return this;
    }

    public PortalBuilder setGateBuilder(@NotNull GateBuilder gateBuilder) {
        this.gateBuilder = Objects.requireNonNull(gateBuilder);
        this.gateAPI = null;
        return this;
    }

    /**
     * Set the flags of the portal
     *
     * @param flagsString <p>A string of characters representing flags or flag arguments</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setFlags(@NotNull String flagsString) {
        this.flagsString = flagsString;
        this.flags = null;
        return this;
    }

    /**
     * Set the flags of the portal
     *
     * @param flags <p>The flags of the portal</p>
     * @return <p>This portal builder</p>
     */
    public PortalBuilder setFlags(@NotNull Set<PortalFlag> flags) {
        this.flags = new HashSet<>(Objects.requireNonNull(flags));
        this.flagsString = null;
        return this;
    }

    /**
     * Build an instance of a portal
     *
     * @return <p>An instance of a portal</p>
     * @throws TranslatableException
     * @throws GateConflictException
     * @throws NoFormatFoundException
     * @throws InvalidStructureException
     */
    public RealPortal build() throws TranslatableException, GateConflictException, NoFormatFoundException, InvalidStructureException {
        setup();
        Set<Character> unrecognisedFlags = PortalFlag.getUnrecognisedFlags(flagsString == null ? "" : flagsString);
        Set<PortalFlag> disallowedFlags = permissionManager.returnDisallowedFlags(flags);
        if (!disallowedFlags.isEmpty() && messageTarget != null) {
            String unformattedMessage = stargateAPI.getLanguageManager().getWarningMessage(TranslatableMessage.LACKING_FLAGS_PERMISSION);
            MessageUtils.sendMessage(messageTarget, TranslatableMessageFormatter.formatFlags(unformattedMessage, disallowedFlags));
        }
        flags.removeAll(disallowedFlags);
        gateAPI = getOrCreateGate();
        if (network == null) {
            network = stargateAPI.getNetworkManager().selectNetwork(networkString, permissionManager, owner, flags);
        }
        NetworkType.removeNetworkTypeRelatedFlags(flags);
        flags.add(network.getType().getRelatedFlag());
        UUID ownerUUID = network.getType() == NetworkType.PERSONAL ? UUID.fromString(network.getId()) : owner.getUniqueId();
        RealPortal portal = PortalCreationHelper.createPortal(network, portalName, destinationName, serverName, flags, unrecognisedFlags, gateAPI, ownerUUID, stargateAPI, metaData);
        gateAPI.assignPortal(portal);
        permissionAndEventHandling(portal, network);

        flagChecks(flags);
        economyCheck(portal);
        finalChecks(portal, network);
        getLocationsAdjacentToPortal(gateAPI).forEach(position -> stargateAPI.getMaterialHandlerResolver().registerPlacement(stargateAPI.getRegistry(), position, List.of(portal), position.getBlock().getType(), eventTarget));
        //Save the portal and inform the user
        stargateAPI.getNetworkManager().savePortal(portal, network);
        gateAPI.getPortalPositions().stream().filter(portalPosition -> portalPosition.getPositionType() == PositionType.SIGN)
                .forEach(portalPosition -> portal.setSignColor(ColorRegistry.DEFAULT_DYE_COLOR, portalPosition));
        Stargate.log(Level.FINE, "Successfully created a new portal");
        String msg;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            msg = stargateAPI.getLanguageManager().getMessage(TranslatableMessage.CREATE_PERSONAL);
        } else {
            String unformattedMessage = stargateAPI.getLanguageManager().getMessage(TranslatableMessage.CREATE);
            msg = TranslatableMessageFormatter.formatNetwork(unformattedMessage, network.getName());
        }
        if (flags.contains(PortalFlag.INTERSERVER)) {
            msg = msg + " " + stargateAPI.getLanguageManager().getString(TranslatableMessage.UNIMPLEMENTED_INTER_SERVER);
        }
        MessageUtils.sendMessageFromPortal(portal, messageTarget, msg, MessageType.CREATE);
        return portal;
    }

    private void setup() {
        if (gateBuilder == null && gateAPI == null) {
            throw new IllegalStateException("Can not construct a portal without a valid gate or gate builder.");
        }
        if (this.flags == null) {
            flags = PortalFlag.parseFlags(flagsString == null ? "" : flagsString);
        }
        //Prevent the player from explicitly setting any internal flags
        flags.removeIf(PortalFlag::isInternalFlag);
        if (destinationName == null || destinationName.isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }
        if (permissionManager == null) {
            permissionManager = new UnrestricedPermissionManager();
        }
    }

    private GateAPI getOrCreateGate() throws GateConflictException, InvalidStructureException, NoFormatFoundException {
        try {
            if (gateAPI == null) {
                if (adaptiveGatePositionGeneration) {
                    gateBuilder.setGenerateButtonPositions(!flags.contains(PortalFlag.ALWAYS_ON));
                }
                return gateBuilder.build();
            }
            return gateAPI;
        } catch (GateConflictException e) {
            MessageUtils.sendMessage(messageTarget, stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.GATE_CONFLICT));
            throw e;
        }
    }

    private void finalChecks(RealPortal portal, Network network) {
        // Warn the player if their portal is interfering with spawn protection
        if (SpawnDetectionHelper.isInterferingWithSpawnProtection(gateAPI)) {
            MessageUtils.sendMessage(messageTarget, stargateAPI.getLanguageManager().getWarningMessage(TranslatableMessage.SPAWN_CHUNKS_CONFLICTING));
        }
        if (portal.hasFlag(PortalFlag.INTERSERVER)) {
            Network inflictingNetwork = NetworkCreationHelper.getInterServerLocalConflict(network, stargateAPI.getRegistry());
            MessageUtils.sendMessage(messageTarget, TranslatableMessageFormatter.formatUnimplementedConflictMessage(network,
                    inflictingNetwork, stargateAPI.getLanguageManager()));
        }
    }

    private void economyCheck(RealPortal portal) throws LocalisedMessageException {
        //Charge the player as necessary for the portal creation
        if (economyTarget != null && EconomyHelper.shouldChargePlayer(economyTarget, portal, BypassPermission.COST_CREATE) &&
                !stargateAPI.getEconomyManager().chargePlayer(economyTarget, null, cost)) {
            String message = stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.LACKING_FUNDS);
            MessageUtils.sendMessageFromPortal(portal, economyTarget, message, MessageType.DENY);
            throw new LocalisedMessageException(message, portal, MessageType.DENY);
        }
    }


    private void flagChecks(Set<PortalFlag> flags) throws TranslatableException {
        //Display an error if trying to create portals across servers while the feature is disabled
        if ((flags.contains(PortalFlag.LEGACY_INTERSERVER) || flags.contains(PortalFlag.INTERSERVER))
                && !ConfigurationHelper.getBoolean(ConfigurationOption.USING_BUNGEE)) {
            MessageUtils.sendMessage(messageTarget, stargateAPI.getLanguageManager().getWarningMessage(TranslatableMessage.BUNGEE_DISABLED));
            throw new TranslatableException("Bungee is disabled") {
                @Override
                protected TranslatableMessage getTranslatableMessage() {
                    return TranslatableMessage.BUNGEE_DISABLED;
                }
            };
        }
        if (flags.contains(PortalFlag.INTERSERVER) && !ConfigurationHelper.getBoolean(
                ConfigurationOption.USING_REMOTE_DATABASE)) {
            MessageUtils.sendMessage(messageTarget, stargateAPI.getLanguageManager().getWarningMessage(TranslatableMessage.INTER_SERVER_DISABLED));
            throw new TranslatableException("Bungee networks are disabled") {
                @Override
                protected TranslatableMessage getTranslatableMessage() {
                    return TranslatableMessage.INTER_SERVER_DISABLED;
                }
            };
        }
    }

    private void permissionAndEventHandling(RealPortal portal, Network network) throws LocalisedMessageException {
        boolean hasPermission = permissionManager == null || permissionManager.hasCreatePermissions(portal);
        if (eventTarget == null) {
            if (hasPermission) {
                return;
            }
            MessageUtils.sendMessage(permissionTarget, permissionManager.getDenyMessage());
            throw new LocalisedMessageException(permissionManager.getDenyMessage(), portal, MessageType.DENY);
        }
        String[] lines = new String[]{this.portalName, destinationName == null ? "" : destinationName, network.getName(), flagsString};
        StargateCreatePortalEvent portalCreateEvent = new StargateCreatePortalEvent(eventTarget, portal, lines, !hasPermission, permissionManager == null ? "" : permissionManager.getDenyMessage(), cost);
        Bukkit.getPluginManager().callEvent(portalCreateEvent);
        Stargate.log(Level.CONFIG, " player has permission = " + hasPermission);

        //If the create event has been denied, tell the user and abort
        if (portalCreateEvent.getDeny()) {
            Stargate.log(Level.CONFIG, " Event was denied due to lack of permission or an add-on");
            String message = null;
            if (portalCreateEvent.getDenyReason() == null) {
                message = stargateAPI.getLanguageManager().getErrorMessage(TranslatableMessage.ADDON_INTERFERE);
            } else if (!portalCreateEvent.getDenyReason().isEmpty()) {
                message = portalCreateEvent.getDenyReason();
            }
            MessageUtils.sendMessage(eventTarget, message);
            throw new LocalisedMessageException(message, portal, MessageType.DENY);
        }
    }

    private static List<Location> getLocationsAdjacentToPortal(GateAPI gate) {
        Set<BlockLocation> adjacentLocations = new HashSet<>();
        for (BlockLocation blockLocation : gate.getLocations(GateStructureType.FRAME)) {
            for (BlockVector adjacentVector : VectorUtils.getAdjacentRelativePositions()) {
                BlockLocation adjacentLocation = new BlockLocation(blockLocation.getLocation().add(adjacentVector));
                adjacentLocations.add(adjacentLocation);
            }
        }
        List<Location> output = new ArrayList<>();
        for (BlockLocation blockLocation : adjacentLocations) {
            output.add(blockLocation.getLocation());
        }

        return output;
    }
}
