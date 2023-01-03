package org.sgrewritten.stargate.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleFunctionAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * A network containing portals across servers
 */
public class InterServerNetwork extends LocalNetwork {

    /**
     * Instantiates a new inter-server network
     *
     * @param networkName <p>The name of the new inter-server network</p>
     * @param type <p>The type of inter-server network to initialize</p>
     * @throws InvalidNameException <p>If the network name is invalid</p>
     * @throws NameLengthException 
     */
    public InterServerNetwork(String networkName, NetworkType type) throws InvalidNameException, NameLengthException {
        super(networkName,type);
    }

    @Override
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        super.removePortal(portal, removeFromDatabase);

        super.registry.removePortal(portal, StorageType.INTER_SERVER);

        if (removeFromDatabase) {
            updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_REMOVE);
        }
    }

    @Override
    public HighlightingStyle getHighlightingStyle() {
        return HighlightingStyle.SQUARE_BRACKETS;
    }

    @Override
    protected void savePortal(RealPortal portal) {
        /*
         * Save one local partition of every bungee gate on this server. Also save it to the inter-server database, so
         * that it can be seen on other servers
         */
        Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            super.registry.savePortal(portal, StorageType.INTER_SERVER);
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
                    String jsonMessage = BungeeHelper.generateJsonMessage(portal,requestType);
                    Stargate.log(Level.FINER, String.format("Sending bungee message:\n%s", jsonMessage));
                    dataOutputStream.writeUTF(jsonMessage);
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


    @Override
    public StorageType getStorageType() {
        return StorageType.INTER_SERVER;
    }
}
