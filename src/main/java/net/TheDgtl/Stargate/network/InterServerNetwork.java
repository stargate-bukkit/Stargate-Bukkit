package net.TheDgtl.Stargate.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.StargateProtocolRequestType;
import net.TheDgtl.Stargate.actions.ForcibleFunctionAction;
import net.TheDgtl.Stargate.actions.SupplierAction;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;


public class InterServerNetwork extends Network {

    public InterServerNetwork(String netName, Database database, SQLQueryGenerator sqlMaker) throws NameError {
        super(netName, database, sqlMaker);
    }

    public InterServerNetwork(String netName, Database database, SQLQueryGenerator sqlMaker, List<IPortal> portals) throws NameError {
        super(netName, database, sqlMaker);
        for (IPortal portal : portals) {
            addPortal(portal, false);
        }
    }

    @Override
    public void removePortal(IPortal portal, boolean saveToDatabase) {
        super.removePortal(portal, saveToDatabase);


        try {
            removePortalFromDatabase(portal, PortalType.INTER_SERVER);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_REMOVE);
        if (!saveToDatabase)
            return;
        try {
            unregisterFromInterServer(portal);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to update the inter-server network globally on every connected server
     */
    private void updateInterServerNetwork(IPortal portal, StargateProtocolRequestType type) {
        Stargate stargate = Stargate.getPlugin(Stargate.class);

        Function<Boolean, Boolean> action = (forceEnd) -> {
            if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                try {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    DataOutputStream msgData = new DataOutputStream(bao);
                    msgData.writeUTF(Channel.FORWARD.getChannel());
                    msgData.writeUTF("ALL");
                    msgData.writeUTF(Channel.NETWORK_CHANGED.getChannel());
                    JsonObject data = new JsonObject();
                    data.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(type.toString()));
                    data.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getName()));
                    data.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(portal.getName()));
                    data.add(StargateProtocolProperty.SERVER.toString(), new JsonPrimitive(Stargate.serverName));
                    data.add(StargateProtocolProperty.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
                    data.add(StargateProtocolProperty.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
                    msgData.writeUTF(data.toString());
                    Bukkit.getServer().sendPluginMessage(stargate, Channel.BUNGEE.getChannel(), bao.toByteArray());
                } catch (IOException ex) {
                    Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord connect packet");
                    ex.printStackTrace();
                }
                return true;
            }
            return false;
        };
        Stargate.syncSecPopulator.addAction(new ForcibleFunctionAction(action), true);
    }

    public void unregisterFromInterServer(IPortal portal) throws SQLException {

    }

    @Override
    public String concatName() {
        return NameSurround.BUNGEE.getSurround(getName());
    }

    @Override
    protected void savePortal(IPortal portal) {
        /*
         * Save one local partition of every bungee gate on this server
         * Also save it to the inter-server database, so that it can be
         * seen on other servers
         */
        Supplier<Boolean> action = () -> {
            savePortal(database, portal, PortalType.INTER_SERVER);
            return true;
        };
        Stargate.syncSecPopulator.addAction(new SupplierAction(action), true);
        updateInterServerNetwork(portal, StargateProtocolRequestType.PORTAL_ADD);
    }
}
