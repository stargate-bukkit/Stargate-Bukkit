package net.TheDgtl.Stargate.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.TheDgtl.Stargate.PluginChannel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.StargateProtocolRequestType;
import net.TheDgtl.Stargate.actions.ForcibleFunctionAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * A network containing portals across servers
 */
public class InterServerNetwork extends Network {

    /**
     * Instantiates a new inter-server network
     *
     * @param networkName    <p>The name of the new inter-server network</p>
     * @param database       <p>The database to use for saving network data</p>
     * @param queryGenerator <p>The generator to use for generating SQL queries</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public InterServerNetwork(String networkName, Database database, SQLQueryGenerator queryGenerator, StargateRegistry registry)
            throws NameErrorException {
        super(networkName, database, queryGenerator, registry);
    }

    @Override
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        super.removePortal(portal, removeFromDatabase);

        try {
            removePortalFromDatabase(portal, PortalType.INTER_SERVER);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_REMOVE);

    }

    @Override
    public String getHighlightedName() {
        return HighlightingStyle.BUNGEE.getHighlightedName(getName());
    }

    @Override
    protected void savePortal(RealPortal portal) {
        /*
         * Save one local partition of every bungee gate on this server. Also save it to the inter-server database, so
         * that it can be seen on other servers
         */
        Stargate.syncSecPopulator.addAction(new SupplierAction(() -> {
            savePortal(database, portal, PortalType.INTER_SERVER);
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

        Stargate.syncSecPopulator.addAction(new ForcibleFunctionAction((forceEnd) -> {
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
                    jsonData.add(StargateProtocolProperty.SERVER.toString(), new JsonPrimitive(Stargate.serverName));
                    jsonData.add(StargateProtocolProperty.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
                    jsonData.add(StargateProtocolProperty.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
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
