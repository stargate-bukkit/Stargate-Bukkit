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
