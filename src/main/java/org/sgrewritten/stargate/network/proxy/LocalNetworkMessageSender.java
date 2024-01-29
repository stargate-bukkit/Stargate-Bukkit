package org.sgrewritten.stargate.network.proxy;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.proxy.PluginMessageSender;

/**
 * Currently does not send any messages to the other servers connected to the proxy
 */
public class LocalNetworkMessageSender implements PluginMessageSender {
    @Override
    public void sendCreatePortal(RealPortal realPortal) {
        // Currently does not need to relay any information cross servers
    }

    @Override
    public void sendDeletePortal(RealPortal realPortal) {
        // Currently does not need to relay any information cross servers
    }

    @Override
    public void sendRenameNetwork(String newId, String oldId) {
        // Currently does not need to relay any information cross servers
    }

    @Override
    public void sendRenamePortal(String newName, String oldName, Network network) {
        // Currently does not need to relay any information cross servers
    }
}
