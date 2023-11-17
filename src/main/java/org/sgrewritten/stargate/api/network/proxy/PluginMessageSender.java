package org.sgrewritten.stargate.api.network.proxy;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

public interface PluginMessageSender {

    void sendCreatePortal(RealPortal realPortal);

    void sendDeletePortal(RealPortal realPortal);

    void sendRenameNetwork(String newId, String oldId);

    void sendRenamePortal(String newName, String oldName, Network network);
}
