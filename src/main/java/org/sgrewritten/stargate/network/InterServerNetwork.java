package org.sgrewritten.stargate.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleFunctionAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * A network containing portals across servers
 */
public class InterServerNetwork extends LocalNetwork {

    /**
     * Instantiates a new inter-server network
     *
     * @param networkName <p>The name of the new inter-server network</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public InterServerNetwork(String networkName) throws NameErrorException {
        super(networkName);
    }

    @Override
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        super.removePortal(portal, removeFromDatabase);

        Stargate.getRegistryStatic().removePortal(portal, PortalType.INTER_SERVER);

        if (removeFromDatabase) {
            updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_REMOVE);
        }
    }

    @Override
    public HighlightingStyle getHighlightingStyle() {
        return HighlightingStyle.BUNGEE;
    }

    @Override
    protected void savePortal(RealPortal portal) {
        /*
         * Save one local partition of every bungee gate on this server. Also save it to the inter-server database, so
         * that it can be seen on other servers
         */
        Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            Stargate.getRegistryStatic().savePortal(portal, PortalType.INTER_SERVER);
            return true;
        }), true);
        updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_ADD);
    }

    /**
     * Tries to update a portal the inter-server network globally on every connected server
     *
     * <p>Basically tells all connected servers to either add or remove the given portal.</p>
     *
     * @param portal      <p>The portal to send to all servers</p>
     * @param requestType <p>The type request to send to the server</p>
     */
    private void updateInterServerNetwork(Portal portal, StargateProtocolRequestType requestType) {
        Stargate stargate = Stargate.getPlugin(Stargate.class);

        Stargate.addSynchronousSecAction(new ForcibleFunctionAction((forceEnd) -> {
            if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                    dataOutputStream.writeUTF(PluginChannel.FORWARD.getChannel());
                    dataOutputStream.writeUTF("ALL");
                    dataOutputStream.writeUTF(PluginChannel.NETWORK_CHANGED.getChannel());
                    JsonObject jsonData = new JsonObject();
                    jsonData.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(requestType.toString()));
                    jsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getName()));
                    jsonData.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(portal.getName()));
                    jsonData.add(StargateProtocolProperty.SERVER.toString(), new JsonPrimitive(Stargate.getServerName()));
                    jsonData.add(StargateProtocolProperty.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
                    jsonData.add(StargateProtocolProperty.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
                    Stargate.log(Level.FINER, String.format("Sending bungee message:\n%s", jsonData));
                    dataOutputStream.writeUTF(jsonData.toString());
                    Bukkit.getServer().sendPluginMessage(stargate, PluginChannel.BUNGEE.getChannel(), byteArrayOutputStream.toByteArray());
                } catch (IOException ex) {
                    Stargate.log(Level.WARNING, "[Stargate] Error sending BungeeCord connect packet");
                    ex.printStackTrace();
                }
                return true;
            }
            return false;

        }), true);
    }

}
