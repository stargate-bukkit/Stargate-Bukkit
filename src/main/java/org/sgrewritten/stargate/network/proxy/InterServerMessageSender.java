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
        String message = BungeeHelper.generateRenamePortalMessage(newId,oldId);
        updateInterServerNetwork(message);
    }

    @Override
    public void sendRenamePortal(String newName, String oldName, Network network) {
        String message = BungeeHelper.generateRenamePortalMessage(newName,oldName,network);
        updateInterServerNetwork(message);
    }

    /**
     * Tries to update a portal the inter-server network globally on every connected server
     *
     * <p>Basically tells all connected servers to either add or remove the given portal.</p>
     *
     * @param message     <p>The message to send</p>
     */
    private void updateInterServerNetwork(String message) {
        Stargate stargate = Stargate.getPlugin(Stargate.class);

        Stargate.addSynchronousSecAction(new ForcibleFunctionAction((forceEnd) -> {
            if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                try {
                    BungeeHelper.sendMessageFromChannel(message, PluginChannel.NETWORK_CHANGED, stargate);
                    return true;
                } catch (IOException e) {
                    Stargate.log(Level.WARNING, "Error sending BungeeCord connect packet");
                    Stargate.log(e);
                }
            }
            return false;

        }), true);
    }
}
