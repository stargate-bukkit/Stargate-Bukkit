package net.TheDgtl.Stargate.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocol;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;


public class InterserverNetwork extends Network {
    private Database interserverDatabase;

    public InterserverNetwork(String netName, Database database, SQLQuerryMaker sqlMaker) throws NameError {
        super(netName, database, sqlMaker);
    }

    public InterserverNetwork(String netName, Database database, SQLQuerryMaker sqlMaker, List<IPortal> portals) throws NameError {
        super(netName, database, sqlMaker);
        for (IPortal portal : portals)
            addPortal(portal, false);
    }

    @Override
    public void removePortal(IPortal portal, boolean saveToDatabase) {
        super.removePortal(portal, saveToDatabase, SQLQuerryMaker.Type.BUNGEE);
        if (!saveToDatabase)
            return;
        try {
            unregisterFromInterserver(portal);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerToInterserver(IPortal portal) {
        PopulatorAction action = new PopulatorAction() {

            @Override
            public void run(boolean forceEnd) {
                savePortal(interserverDatabase, portal, SQLQuerryMaker.Type.INTERSERVER);
            }

            @Override
            public boolean isFinished() {
                return true;
            }

        };
        Stargate.syncSecPopulator.addAction(action, true);
        updateInterserverNetwork(portal, StargateProtocol.TYPE_PORTAL_ADD);
    }

    /**
     * Tries to update the interserver network globally on every connected server
     */
    private void updateInterserverNetwork(IPortal portal, StargateProtocol type) {
        Stargate stargate = Stargate.getPlugin(Stargate.class);
        PopulatorAction action = new PopulatorAction() {
            boolean isFinished = false;

            @Override
            public void run(boolean forceEnd) {
                if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                    isFinished = true;
                    try {
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        DataOutputStream msgData = new DataOutputStream(bao);
                        msgData.writeUTF(Channel.FORWARD.getChannel());
                        msgData.writeUTF("ALL");
                        msgData.writeUTF(Channel.NETWORK_CHANGED.getChannel());
                        JsonObject data = new JsonObject();
                        data.add(StargateProtocol.TYPE.toString(), new JsonPrimitive(type.toString()));
                        data.add(StargateProtocol.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getName()));
                        data.add(StargateProtocol.PORTAL.toString(), new JsonPrimitive(portal.getName()));
                        data.add(StargateProtocol.SERVER.toString(), new JsonPrimitive(Stargate.serverName));
                        data.add(StargateProtocol.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
                        data.add(StargateProtocol.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
                        msgData.writeUTF(data.toString());
                        Bukkit.getServer().sendPluginMessage(stargate, Channel.BUNGEE.getChannel(), bao.toByteArray());
                    } catch (IOException ex) {
                        Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord connect packet");
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public boolean isFinished() {
                return isFinished;
            }
        };
        Stargate.syncSecPopulator.addAction(action, true);
    }

    public void unregisterFromInterserver(IPortal portal) throws SQLException {
        Connection conn = interserverDatabase.getConnection();
        PreparedStatement statement = sqlMaker.compileRemoveStatement(conn, portal, SQLQuerryMaker.Type.INTERSERVER);
        statement.execute();
        statement.close();
        conn.close();

        updateInterserverNetwork(portal, StargateProtocol.TYPE_PORTAL_REMOVE);
    }

    @Override
    public String concatName() {
        return NameSurround.BUNGEE.getSurround(getName());
    }

    @Override
    protected void savePortal(IPortal portal) {
        /*
         * Save one local partition of every bungee gate on this server
         * Also save it to the interserver database, so that it can be
         * seen on other servers
         */
        super.savePortal(database, portal, SQLQuerryMaker.Type.BUNGEE);
        registerToInterserver(portal);
    }
}
