package org.sgrewritten.stargate.network.proxy;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleFunctionAction;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.proxy.PluginMessageSender;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;

import java.io.IOException;
import java.util.logging.Level;

public class InterServerMessageSender implements PluginMessageSender {

    private final PluginMessageInterface pluginMessageInterface;

    public InterServerMessageSender(){
        this.pluginMessageInterface = new BukkitPluginMessageInterface();
    }

    public InterServerMessageSender(PluginMessageInterface pluginMessageInterface){
        this.pluginMessageInterface = pluginMessageInterface;
    }

    @Override
    public void sendCreatePortal(RealPortal realPortal) {
        String message = BungeeHelper.generateJsonMessage(realPortal, StargateProtocolRequestType.PORTAL_ADD);
        updateInterServerNetwork(message);
    }

    @Override
    public void sendDeletePortal(RealPortal realPortal) {
        String message = BungeeHelper.generateJsonMessage(realPortal, StargateProtocolRequestType.PORTAL_REMOVE);
        updateInterServerNetwork(message);
    }

    @Override
    public void sendRenameNetwork(String newId, String oldId) {
        String message = BungeeHelper.generateRenameNetworkMessage(newId,oldId);
        updateInterServerNetwork(message);
    }

    @Override
    public void sendRenamePortal(String newName, String oldName, Network network) {
        String message = BungeeHelper.generateRenamePortalMessage(newName,oldName,network);
        updateInterServerNetwork(message);
    }

    /**
     * Send messages to all servers connected to the BungeeCoord proxy. (requires a player
     * to be online on both ends does not need to be simultaneously)
     * @param message     <p>The message to send</p>
     */
    private void updateInterServerNetwork(String message) {
        this.pluginMessageInterface.scheduleSendMessage(message, PluginChannel.NETWORK_CHANGED);
    }
}
