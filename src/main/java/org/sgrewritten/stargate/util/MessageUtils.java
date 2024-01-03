package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.message.AsyncStargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.event.portal.message.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.SyncStargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class MessageUtils {

    public static void sendMessageFromPortal(Portal portal, @Nullable Entity receiver, String message, MessageType type){
        if(message == null || message.isBlank() || receiver == null){
            return;
        }
        StargateSendMessagePortalEvent event;
        if(Bukkit.isPrimaryThread()){
            event = new SyncStargateSendMessagePortalEvent(portal,receiver,type);
        } else {
            event = new AsyncStargateSendMessagePortalEvent(portal,receiver,type);
        }
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()){
            receiver.sendMessage(message);
        }
    }

    public static void sendMessage(Entity receiver, String message){
        receiver.sendMessage(message);
    }
}
