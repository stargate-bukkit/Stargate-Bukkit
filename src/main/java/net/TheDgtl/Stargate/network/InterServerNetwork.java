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
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
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

    public InterServerNetwork(String netName, Database database, SQLQueryGenerator sqlMaker) throws NameErrorException {
        super(netName, database, sqlMaker);
    }

    public InterServerNetwork(String netName, Database database, SQLQueryGenerator sqlMaker, List<Portal> portals) throws NameErrorException {
        super(netName, database, sqlMaker);
        for (Portal portal : portals) {
            addPortal(portal, false);
        }
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
        if (!removeFromDatabase)
            return;
    }

    /**
     * Tries to update the inter-server network globally on every connected server
     */
    private void updateInterServerNetwork(Portal portal, StargateProtocolRequestType type) {
        Stargate stargate = Stargate.getPlugin(Stargate.class);

        Function<Boolean, Boolean> action = (forceEnd) -> {
            if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                try {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    DataOutputStream msgData = new DataOutputStream(bao);
                    msgData.writeUTF(PluginChannel.FORWARD.getChannel());
                    msgData.writeUTF("ALL");
                    msgData.writeUTF(PluginChannel.NETWORK_CHANGED.getChannel());
                    JsonObject data = new JsonObject();
                    data.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(type.toString()));
                    data.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getName()));
                    data.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(portal.getName()));
                    data.add(StargateProtocolProperty.SERVER.toString(), new JsonPrimitive(Stargate.serverName));
                    data.add(StargateProtocolProperty.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
                    data.add(StargateProtocolProperty.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
                    msgData.writeUTF(data.toString());
                    Bukkit.getServer().sendPluginMessage(stargate, PluginChannel.BUNGEE.getChannel(), bao.toByteArray());
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

    @Override
    public String getHighlightedName() {
        return HighlightingStyle.BUNGEE.getHighlightedName(getName());
    }

    @Override
    protected void savePortal(Portal portal) {
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
