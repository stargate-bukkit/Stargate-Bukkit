package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.StargateMessageEvent;
import org.sgrewritten.stargate.api.event.portal.message.AsyncStargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.event.portal.message.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.SyncStargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.AdventureStargateComponent;
import org.sgrewritten.stargate.api.network.portal.formatting.LegacyStargateComponent;
import org.sgrewritten.stargate.api.network.portal.formatting.StargateComponent;
import org.sgrewritten.stargate.property.NonLegacyClass;

public class MessageUtils {
    private MessageUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Send a message from this portal; triggers a {@link StargateSendMessagePortalEvent}
     * @param portal <p>The portal that relates to this message</p>
     * @param receiver <p>The receiving entity of this message</p>
     * @param message <p>The legacy serialized message to send</p>
     * @param type <p>The type of the message (see {@link MessageType})</p>
     */
    public static void sendMessageFromPortal(Portal portal, @Nullable Entity receiver, String message, MessageType type) {
        if (message == null || message.isBlank() || receiver == null) {
            return;
        }
        StargateComponent component = new LegacyStargateComponent(message);
        StargateSendMessagePortalEvent event;
        if (Bukkit.isPrimaryThread()) {
            event = new SyncStargateSendMessagePortalEvent(portal, receiver, type, component);
        } else {
            event = new AsyncStargateSendMessagePortalEvent(portal, receiver, type, component);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.getMessage().value.sendMessage(receiver);
        }
    }

    /**
     * Send a message that does not belong to any portal, will trigger a {@link StargateMessageEvent}
     * @param receiver <p>The receiver of the message</p>
     * @param message <p>The legacy serialized message</p>
     */
    public static void sendMessage(@Nullable Entity receiver, String message) {
        if (receiver == null){
            return;
        }
        StargateComponent component = new LegacyStargateComponent(message);
        StargateMessageEvent event = new StargateMessageEvent(component);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.getMessage().sendMessage(receiver);
        }
    }
}
