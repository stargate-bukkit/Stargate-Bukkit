package org.sgrewritten.stargate.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.gate.GateBuilder;
import org.sgrewritten.stargate.api.network.PortalBuilder;

import java.util.Objects;

/**
 * <p>Is triggered before the {@link org.sgrewritten.stargate.api.event.portal.StargateCreatePortalEvent}.</p>
 */
public class StargatePreCreatePortalEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final PortalBuilder portalBuilder;
    private final GateBuilder gateBuilder;
    private final String[] args;
    private final Player player;
    private boolean cancelled = false;


    /**
     *
     * @param portalBuilder <p>The portal builder that is going to build the portal</p>
     * @param gateBuilder <p>The gate builder which is going to build the gate</p>
     * @param args <p>The sign arguments, has to have an length of aat least 4</p>
     * @param player <p>The player that initiated the event</p>
     */
    public StargatePreCreatePortalEvent(@NotNull PortalBuilder portalBuilder, @NotNull GateBuilder gateBuilder, @NotNull String[] args, @Nullable Player player) {
        this.portalBuilder = Objects.requireNonNull(portalBuilder);
        this.gateBuilder = Objects.requireNonNull(gateBuilder);
        if (args.length < 4) {
            throw new IllegalArgumentException("Expected at least 4 input arguments");
        }
        this.args = Objects.requireNonNull(args);
        this.player = player;
    }

    public @NotNull PortalBuilder getPortalBuilder() {
        return this.portalBuilder;
    }

    public @NotNull GateBuilder getGateBuilder() {
        return this.gateBuilder;
    }

    /**
     * @return <p>The name for portal in the portal creation attempt</p>
     */
    public @NotNull String getPortalName() {
        return args[0];
    }

    /**
     * The name of the destination, might be empty if no destination was selected
     *
     * @return <p>The name of the destination</p>
     */
    public @NotNull String getDestinationName() {
        return args[1];
    }

    /**
     * Do note that this network name will be heavily modified from permission checks.
     *
     * @return <p>The initial name of the network</p>
     */
    public @NotNull String getNetworkName() {
        return args[2];
    }

    /**
     * Note that the resulting flags might heavily be changed from this string. Any string
     * matching the pattern (\{.*?\}) will be ignored, but can be used for addons as flag
     * arguments.
     *
     * @return <p>The initial string representing flags</p>
     */
    public @NotNull String getFlagString() {
        return args[3];
    }

    /**
     * @return <p>The player that initiated the event</p>
     */
    public @Nullable Player getPlayer() {
        return player;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
