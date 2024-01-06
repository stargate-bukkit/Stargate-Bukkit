package org.sgrewritten.stargate.api.network.proxy;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

public interface PluginMessageSender {

    /**
     * Send create portal message
     * @param realPortal <p>The portal which data will be sent</p>
     */
    void sendCreatePortal(RealPortal realPortal);

    /**
     * Send delete portal message
     * @param realPortal <p>The portal which data will be deleted</p>
     */
    void sendDeletePortal(RealPortal realPortal);

    /**
     * Send rename network message
     * @param newId <p>New id of the network</p>
     * @param oldId <p>Old id of the network</p>
     */
    void sendRenameNetwork(String newId, String oldId);

    /**
     * Send rename portal message
     * @param newName <p>New name of the portal</p>
     * @param oldName <p>Old name of the portal</p>
     * @param network <p>The network of the portal</p>
     */
    void sendRenamePortal(String newName, String oldName, Network network);
}
