package org.sgrewritten.stargate.util.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.PortalLoadException;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.network.portal.portaldata.PortalData;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.util.LegacyDataHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class PortalStorageHelper {

    private PortalStorageHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Load portal data from sql result set
     * @param resultSet <p>Sql result set representing one row in the portals table</p>
     * @param portalType <p>How the portal is stored</p>
     * @return <p>Portal data which can be used to create a portal</p>
     * @throws SQLException <p>If any sql problem</p>
     * @throws PortalLoadException <p>See {@link PortalLoadException.FailureType} for causes</p>
     */
    public static @NotNull PortalData loadPortalData(ResultSet resultSet, StorageType portalType) throws SQLException, PortalLoadException {
        String name = resultSet.getString("name");
        String networkName = resultSet.getString("network");
        String destination = resultSet.getString("destination");
        // Make sure to treat no destination as empty, not a null string
        if (resultSet.wasNull()) {
            destination = "";
        }
        String serverUUID = null;
        String serverName = null;
        if (portalType == StorageType.INTER_SERVER) {
            serverUUID = resultSet.getString("homeServerId");
            Stargate.log(Level.FINEST, "serverUUID = " + serverUUID);
            if (!serverUUID.equals(Stargate.getServerUUID())) {
                serverName = resultSet.getString("serverName");
            }
        }
        World world = Bukkit.getWorld(UUID.fromString(resultSet.getString("world")));
        // Avoid the use of unecessary queries if server is not this server
        if (world == null && serverName == null) {
            Stargate.log(Level.FINE, "World does not exist for portal: " + networkName + ":" + name);
            throw new PortalLoadException(PortalLoadException.FailureType.WORLD);
        }
        Location topLeft = new Location(world, resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));

        String flagString = resultSet.getString("flags");
        Set<PortalFlag> flags = PortalFlag.parseFlags(flagString);
        UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
        String gateFileName = resultSet.getString("gateFileName");
        boolean flipZ = resultSet.getBoolean("flipZ");
        BlockFace facing = getBlockFaceFromOrdinal(Integer.parseInt(resultSet.getString("facing")));
        String metadata = resultSet.getString("metaData");
        GateFormatAPI format = GateFormatRegistry.getFormat(gateFileName);
        if (format == null) {
            Stargate.log(Level.WARNING, String.format("Could not find the format ''%s''. Check the full startup " +
                    "log for more information", gateFileName));
            throw new PortalLoadException(PortalLoadException.FailureType.GATE_FORMAT);
        }
        GateData gateData = new GateData(format, flipZ, topLeft, facing);
        return new PortalData(gateData, name, networkName, destination, flags, ownerUUID, serverUUID, serverName, portalType, metadata);
    }

    /**
     * Load a portal position from sql result set
     * @param resultSet <p>Sql row in table</p>
     * @return <p>A portal position based from the row in the table</p>
     * @throws NumberFormatException <p>IF unable to parse integer</p>
     * @throws SQLException <p>if any sql error</p>
     */
    public static PortalPosition loadPortalPosition(ResultSet resultSet) throws NumberFormatException, SQLException {
        int xCoordinate = Integer.parseInt(resultSet.getString("xCoordinate"));
        int yCoordinate = Integer.parseInt(resultSet.getString("yCoordinate"));
        int zCoordinate = -Integer.parseInt(resultSet.getString("zCoordinate"));
        BlockVector positionVector = new BlockVector(xCoordinate, yCoordinate, zCoordinate);
        PositionType positionType = PositionType.valueOf(resultSet.getString("positionName"));
        String pluginName = resultSet.getString("pluginName");
        // Let the Stargate registered portal positions be active at startup, so that controls can be defaulted if an addon does not assign itself a position
        return new PortalPosition(positionType, positionVector, pluginName, pluginName.equals("Stargate"));
    }


    public static void addPortalPosition(PreparedStatement addPositionStatement, RealPortal portal, PortalPosition portalPosition) throws SQLException {
        Stargate.log(Level.FINEST, "Saving portal position, " + portalPosition + " for portal " + portal.getName() + ":" + portal.getNetwork().getName());
        addPositionStatement.setString(1, portal.getName());
        addPositionStatement.setString(2, portal.getNetwork().getId());
        addPositionStatement.setString(3, String.valueOf(portalPosition.getRelativePositionLocation().getBlockX()));
        addPositionStatement.setString(4, String.valueOf(portalPosition.getRelativePositionLocation().getBlockY()));
        addPositionStatement.setString(5, String.valueOf(-portalPosition.getRelativePositionLocation().getBlockZ()));
        addPositionStatement.setString(6, portalPosition.getPositionType().name());
        addPositionStatement.setString(7, "");
        addPositionStatement.setString(8, portalPosition.getPluginName());
        addPositionStatement.execute();
    }

    /**
     * Load legacy data
     *
     * @param portalProperties <p> A data sequence in the format of legacy storage resembling a portal</p>
     * @param world            <p>The world to load portal data for</p>
     * @return <p>The loaded portal data</p>
     */
    public static PortalData loadPortalData(String[] portalProperties, World world, String defaultNetworkName) {
        String name = portalProperties[0];
        String networkName = (portalProperties.length > 9) ? portalProperties[9] : StargateConstant.DEFAULT_NETWORK_ID;

        Stargate.log(Level.FINEST, String.format("-----------------Loading portal %s in network %s--------------" +
                "--------", name, networkName));
        int modX = Integer.parseInt(portalProperties[3]);
        int modZ = Integer.parseInt(portalProperties[4]);
        double rotation = Double.parseDouble(portalProperties[5]);
        Stargate.log(Level.FINEST, String.format("modX = %d, modZ = %d, rotation %f", modX, modZ, rotation));

        BlockFace facing = LegacyDataHandler.getFacing(modX, modZ);
        if (facing == null) {
            facing = LegacyDataHandler.getFacing(rotation);
        }
        Stargate.log(Level.FINEST, String.format("chose a facing %s", facing.toString()));
        Location topLeft = LegacyDataHandler.loadLocation(world, portalProperties[6]);
        String gateFileName = portalProperties[7];
        String destination = (portalProperties.length > 8) ? portalProperties[8] : "";
        String ownerString = (portalProperties.length > 10) ? portalProperties[10] : "";
        UUID ownerUUID = LegacyDataHandler.getPlayerUUID(ownerString);
        Set<PortalFlag> flags = LegacyDataHandler.parseFlags(portalProperties);
        if (destination == null || destination.trim().isEmpty()) {
            flags.add(StargateFlag.NETWORKED);
        }

        if (portalProperties.length <= 9 || networkName.equalsIgnoreCase(defaultNetworkName)) {
            flags.add(StargateFlag.DEFAULT_NETWORK);
            networkName = StargateConstant.DEFAULT_NETWORK_ID;
        } else if (!ownerString.isEmpty()) {
            String playerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
            if (playerName != null && playerName.equalsIgnoreCase(networkName)) {
                flags.add(StargateFlag.PERSONAL_NETWORK);
                networkName = ownerUUID.toString();
            } else {
                flags.add(StargateFlag.CUSTOM_NETWORK);
            }
        } else {
            flags.add(StargateFlag.CUSTOM_NETWORK);
        }
        GateFormatAPI format = GateFormatRegistry.getFormat(gateFileName);
        if (format == null) {
            Stargate.log(Level.WARNING, String.format("Could not find the format ''%s''. Check the full startup " +
                    "log for more information", gateFileName));
            throw new IllegalArgumentException("Could not find gate format");
        }
        GateData gateData = new GateData(format, false, topLeft, facing);
        return new PortalData(gateData, name, networkName, destination, flags, ownerUUID, null, null, StorageType.LOCAL, null);
    }

    /**
     * Gets the correct block face from the given ordinal
     *
     * @param ordinal <p>The ordinal to get the block face from</p>
     * @return <p>The corresponding block face, or null</p>
     */
    private static BlockFace getBlockFaceFromOrdinal(int ordinal) {
        for (BlockFace blockFace : BlockFace.values()) {
            if (blockFace.ordinal() == ordinal) {
                return blockFace;
            }
        }
        return null;
    }
}
