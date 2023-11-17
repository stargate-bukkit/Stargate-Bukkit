package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class MessageUtils {

    public static void sendMessageFromPortal(Portal portal, @Nullable Entity receiver, String message, StargateSendMessagePortalEvent.MessageType type){
        if(message == null || message.isBlank() || receiver == null){
            return;
        }
        StargateSendMessagePortalEvent sendMessageEvent = new StargateSendMessagePortalEvent(portal,receiver,type);
        Bukkit.getPluginManager().callEvent(sendMessageEvent);
        if(!sendMessageEvent.isCancelled()){
            receiver.sendMessage(message);
        }
    }

    public static void sendMessage(Entity receiver, String message){
        receiver.sendMessage(message);
    }
}
