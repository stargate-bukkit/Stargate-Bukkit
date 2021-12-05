package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DriverEnum;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.database.TableNameConfig;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StargateFactory {


    private String PREFIX = "";
    private final HashMap<String, Network> networkList = new HashMap<>();
    private final HashMap<String, InterServerNetwork> bungeeNetList = new HashMap<>();

    final String sharedTableName = "interServer";
    final String bungeeDataBaseName = "bungee";
    final String tableName = "local";

    private final Database database;

    private final SQLQueryGenerator sqlMaker;
    private final boolean useInterServerNetworks;

    public StargateFactory(Stargate stargate) throws SQLException {
        database = loadDatabase(stargate);
        useInterServerNetworks = (Settings.getBoolean(Setting.USING_REMOTE_DATABASE) && Settings.getBoolean(Setting.USING_BUNGEE));
        PREFIX = Settings.getString(Setting.BUNGEE_INSTANCE_NAME);
        TableNameConfig config = new TableNameConfig("SG_", "");
        DriverEnum databaseEnum = Settings.getBoolean(Setting.USING_REMOTE_DATABASE) ? DriverEnum.MYSQL : DriverEnum.SQLITE;
        this.sqlMaker = new SQLQueryGenerator(config, Stargate.getInstance(), databaseEnum);
        createTables();


        Stargate.log(Level.FINER, "Loading portals from base database");
        loadAllPortals(database, PortalType.LOCAL);
        if (useInterServerNetworks) {
            Stargate.log(Level.FINER, "Loading portals from inter-server bungee database");
            loadAllPortals(database, PortalType.INTER_SERVER);
        }

        refreshPortals(networkList);
        refreshPortals(bungeeNetList);
    }

    private Database loadDatabase(Stargate stargate) throws SQLException {
        if (Settings.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            if (Settings.getBoolean(Setting.SHOW_HIKARI_CONFIG))
                return new MySqlDatabase(stargate);

            DriverEnum driver = DriverEnum.valueOf(Settings.getString(Setting.BUNGEE_DRIVER).toUpperCase());
            String bungeeDatabaseName = Settings.getString(Setting.BUNGEE_DATABASE);
            int port = Settings.getInteger(Setting.BUNGEE_PORT);
            String address = Settings.getString(Setting.BUNGEE_ADDRESS);
            String username = Settings.getString(Setting.BUNGEE_USERNAME);
            String password = Settings.getString(Setting.BUNGEE_PASSWORD);
            boolean useSSL = Settings.getBoolean(Setting.BUNGEE_USE_SSL);

            switch (driver) {
                case MARIADB:
                case MYSQL:
                    return new MySqlDatabase(driver, address, port, bungeeDatabaseName, username, password, useSSL);
                default:
                    throw new SQLException("Unsupported driver: Stargate currently supports MariaDb and MySql for remote databases");
            }
        } else {
            String databaseName = Settings.getString(Setting.DATABASE_NAME);
            File file = new File(stargate.getDataFolder().getAbsoluteFile(), databaseName + ".db");
            return new SQLiteDatabase(file);
        }
    }

    private void refreshPortals(HashMap<String, ? extends Network> networksList) {
        for (Network net : networksList.values()) {
            net.updatePortals();
        }
    }

    private void runStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
        statement.close();
    }

    /**
     * Creates all necessary database tables
     *
     * @throws SQLException <p>If an SQL exception occurs</p>
     */
    private void createTables() throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement localPortalsStatement = sqlMaker.generateCreatePortalTableStatement(connection, PortalType.LOCAL);
        runStatement(localPortalsStatement);
        PreparedStatement flagStatement = sqlMaker.generateCreateFlagTableStatement(connection);
        runStatement(flagStatement);
        addMissingFlags(connection, sqlMaker);

        PreparedStatement serverInfoStatement = sqlMaker.generateCreateServerInfoTableStatement(connection);
        runStatement(serverInfoStatement);

        PreparedStatement lastKnownNameStatement = sqlMaker.generateCreateLastKnownNameTableStatement(connection);
        runStatement(lastKnownNameStatement);
        PreparedStatement portalRelationStatement = sqlMaker.generateCreateFlagRelationTableStatement(connection, PortalType.LOCAL);
        runStatement(portalRelationStatement);
        PreparedStatement portalViewStatement = sqlMaker.generateCreatePortalViewStatement(connection, PortalType.LOCAL);
        runStatement(portalViewStatement);

        if (!useInterServerNetworks) {
            connection.close();
            return;
        }

        PreparedStatement interServerPortalsStatement = sqlMaker.generateCreatePortalTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerPortalsStatement);
        PreparedStatement interServerRelationStatement = sqlMaker.generateCreateFlagRelationTableStatement(connection, PortalType.INTER_SERVER);
        runStatement(interServerRelationStatement);
        PreparedStatement interPortalViewStatement = sqlMaker.generateCreatePortalViewStatement(connection, PortalType.INTER_SERVER);
        runStatement(interPortalViewStatement);
        connection.close();
    }

    /**
     * Adds any flags not already in the database
     *
     * @param connection <p>The database connection to use</p>
     * @param sqlMaker   <p>The SQL Query Generator to use for generating queries</p>
     * @throws SQLException <p>If unable to get from, or update the database</p>
     */
    private void addMissingFlags(Connection connection, SQLQueryGenerator sqlMaker) throws SQLException {
        PreparedStatement statement = sqlMaker.generateGetAllFlagsStatement(connection);
        PreparedStatement addStatement = sqlMaker.generateAddFlagStatement(connection);

        ResultSet set = statement.executeQuery();
        List<String> knownFlags = new ArrayList<>();
        while (set.next()) {
            knownFlags.add(set.getString("character"));
        }
        for (PortalFlag flag : PortalFlag.values()) {
            if (!knownFlags.contains(String.valueOf(flag.getCharacterRepresentation()))) {
                addStatement.setString(1, String.valueOf(flag.getCharacterRepresentation()));
                addStatement.execute();
            }
        }
        statement.close();
        addStatement.close();
    }

    private void loadAllPortals(Database database, PortalType tablePortalType) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = sqlMaker.generateGetAllPortalsStatement(connection, tablePortalType);

        ResultSet set = statement.executeQuery();
        while (set.next()) {
            String name = set.getString("name");
            String netName = set.getString("network");

            //Skip null rows
            if (name == null && netName == null) {
                continue;
            }

            String destination = set.getString("destination");
            //Make sure to treat no destination as empty, not a null string
            if (set.wasNull()) {
                destination = "";
            }
            String worldName = set.getString("world");
            int x = set.getInt("x");
            int y = set.getInt("y");
            int z = set.getInt("z");
            String flagsMsg = set.getString("flags");
            UUID ownerUUID = UUID.fromString(set.getString("ownerUUID"));

            Set<PortalFlag> flags = PortalFlag.parseFlags(flagsMsg);

            boolean isBungee = flags.contains(PortalFlag.FANCY_INTER_SERVER);
            Stargate.log(Level.FINEST, "Trying to add portal " + name + ", on network " + netName + ",isInterServer = " + isBungee);

            String targetNet = netName;
            if (flags.contains(PortalFlag.BUNGEE)) {
                targetNet = "§§§§§§#BUNGEE#§§§§§§";
            }

            try {
                createNetwork(targetNet, flags);
            } catch (NameErrorException ignored) {
            }
            Network net = getNetwork(targetNet, isBungee);

            for (IPortal logPortal : net.getAllPortals()) {
                Stargate.log(Level.FINEST, logPortal.getName());
            }

            if (tablePortalType == PortalType.INTER_SERVER) {
                String serverUUID = set.getString("homeServerId");
                Stargate.log(Level.FINEST, "serverUUID = " + serverUUID);
                if (!serverUUID.equals(Stargate.serverUUID.toString())) {
                    String serverName = set.getString("serverName");
                    IPortal virtualPortal = new VirtualPortal(serverName, name, net, flags, ownerUUID);
                    net.addPortal(virtualPortal, false);
                    Stargate.log(Level.FINEST, "Added as virtual portal");
                    continue;
                }
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }
            Block block = world.getBlockAt(x, y, z);
            String[] virtualSign = {name, destination, netName};


            try {
                IPortal portal = Portal.createPortalFromSign(net, virtualSign, block, flags, ownerUUID);
                net.addPortal(portal, false);
                Stargate.log(Level.FINEST, "Added as normal portal");
                if (isBungee) {
                    setInterServerPortalOnlineStatus(portal, true);
                }
            } catch (GateConflictException | NoFormatFoundException ignored) {
            } catch (NameErrorException e) {
                e.printStackTrace();
            }
        }
        statement.close();
        connection.close();
    }

    private void setInterServerPortalOnlineStatus(IPortal portal, boolean isOnline) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlMaker.generateSetPortalOnlineStatusStatement(conn, portal, isOnline);
        statement.execute();
        statement.close();
        conn.close();
    }

    public void startInterServerConnection() throws SQLException {
        Connection conn = database.getConnection();
        for (Network net : bungeeNetList.values()) {
            for (IPortal portal : net.getAllPortals()) {
                if (portal instanceof VirtualPortal)
                    continue;
                PreparedStatement statement = sqlMaker.generateUpdateServerInfoStatus(conn, Stargate.serverName, Stargate.serverUUID, PREFIX);
                statement.execute();
                statement.close();
            }
        }
        conn.close();
    }

    public void endInterServerConnection() throws SQLException {
        for (InterServerNetwork net : bungeeNetList.values()) {
            for (IPortal portal : net.getAllPortals()) {
                /*
                 * Virtual portal = portals on other servers
                 */
                if (portal instanceof VirtualPortal)
                    continue;

                setInterServerPortalOnlineStatus(portal, false);
            }
        }
    }

    public void createNetwork(String netName, Set<PortalFlag> flags) throws NameErrorException {
        if (netExists(netName, flags.contains(PortalFlag.FANCY_INTER_SERVER)))
            throw new NameErrorException(null);
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            InterServerNetwork net = new InterServerNetwork(netName, database, sqlMaker);
            String netHash = net.getName().toLowerCase();
            if (Settings.getBoolean(Setting.DISABLE_CUSTOM_COLORED_NAMES)) {
                netHash = ChatColor.stripColor(netHash);
            }
            bungeeNetList.put(netHash, net);
            return;
        }
        Network net;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID id = UUID.fromString(netName);
            net = new PersonalNetwork(id, database, sqlMaker);
        } else {
            net = new Network(netName, database, sqlMaker);
        }
        networkList.put(netName, net);
    }

    public boolean netExists(String netName, boolean isBungee) {
        return (getNetwork(netName, isBungee) != null);
    }

    public Network getNetwork(String name, boolean isBungee) {
        return getNetMap(isBungee).get(name);
    }

    private HashMap<String, ? extends Network> getNetMap(boolean isBungee) {
        if (isBungee) {
            return bungeeNetList;
        } else {
            return networkList;
        }
    }

    final HashMap<String, BungeePortal> bungeeList = new HashMap<>();

    public BungeePortal getBungeeGate(String name) {
        return bungeeList.get(name);
    }

    /**
     * Load portal from one line in legacy database
     *
     * @param str
     * @return
     */
    public IPortal createFromString(String str) {
        return null;
    }
}
