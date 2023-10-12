package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.event.portal.StargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class MessageUtils {

    public static void sendMessageFromPortal(Portal portal, Entity receiver, String message, StargateSendMessagePortalEvent.MessageType type){
        if(message == null || message.isBlank()){
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
