package org.sgrewritten.stargate.util.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.PositionType;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.util.LegacyDataHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PortalStorageHelper {


    public static PortalData loadPortalData(ResultSet resultSet, StorageType portalType) throws SQLException {
        PortalData portalData = new PortalData();
        portalData.name = resultSet.getString("name");
        portalData.networkName = resultSet.getString("network");
        portalData.destination = resultSet.getString("destination");
        // Make sure to treat no destination as empty, not a null string
        if (resultSet.wasNull()) {
            portalData.destination = "";
        }

        World world = Bukkit.getWorld(resultSet.getString("world"));
        if (world == null) {
            return null;
        }
        portalData.topLeft = new Location(world, resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));

        portalData.flagString = resultSet.getString("flags");
        portalData.flags = PortalFlag.parseFlags(portalData.flagString);
        portalData.ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
        portalData.gateFileName = resultSet.getString("gateFileName");
        portalData.flipZ = resultSet.getBoolean("flipZ");
        portalData.facing = getBlockFaceFromOrdinal(Integer.parseInt(resultSet.getString("facing")));
        portalData.portalType = portalType;
        //TODO Check if portalType is necessary to keep track of // there's already flags.contains(PortalFlag.FANCY_INTERSERVER)
        if (portalType == StorageType.INTER_SERVER) {
            portalData.serverUUID = resultSet.getString("homeServerId");
            Stargate.log(Level.FINEST, "serverUUID = " + portalData.serverUUID);
            if (!portalData.serverUUID.equals(Stargate.getServerUUID())) {
                portalData.serverName = resultSet.getString("serverName");
            }
        }

        return portalData;
    }

    public static PortalPosition loadPortalPosition(ResultSet resultSet) throws NumberFormatException, SQLException {
        int xCoordinate = Integer.parseInt(resultSet.getString("xCoordinate"));
        int yCoordinate = Integer.parseInt(resultSet.getString("yCoordinate"));
        int zCoordinate = -Integer.parseInt(resultSet.getString("zCoordinate"));
        BlockVector positionVector = new BlockVector(xCoordinate, yCoordinate, zCoordinate);
        PositionType positionType = PositionType.valueOf(resultSet.getString("positionName"));
        return new PortalPosition(positionType, positionVector);
    }


    public static void addPortalPosition(PreparedStatement addPositionStatement, RealPortal portal, PortalPosition portalPosition) throws SQLException {
        addPositionStatement.setString(1, portal.getName());
        addPositionStatement.setString(2, portal.getNetwork().getId());
        addPositionStatement.setString(3, String.valueOf(portalPosition.getPositionLocation().getBlockX()));
        addPositionStatement.setString(4, String.valueOf(portalPosition.getPositionLocation().getBlockY()));
        addPositionStatement.setString(5, String.valueOf(-portalPosition.getPositionLocation().getBlockZ()));
        addPositionStatement.setString(6, portalPosition.getPositionType().name());
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
        PortalData portalData = new PortalData();
        portalData.name = portalProperties[0];
        portalData.networkName = (portalProperties.length > 9) ? portalProperties[9] : LocalNetwork.DEFAULT_NETWORK_ID;

        Stargate.log(Level.FINEST, String.format("-----------------Loading portal %s in network %s--------------" +
                "--------", portalData.name, portalData.networkName));
        int modX = Integer.parseInt(portalProperties[3]);
        int modZ = Integer.parseInt(portalProperties[4]);
        double rotation = Double.parseDouble(portalProperties[5]);
        Stargate.log(Level.FINEST, String.format("modX = %d, modZ = %d, rotation %f", modX, modZ, rotation));

        portalData.facing = LegacyDataHandler.getFacing(modX, modZ);
        if (portalData.facing == null) {
            portalData.facing = LegacyDataHandler.getFacing(rotation);
        }
        Stargate.log(Level.FINEST, String.format("chose a facing %s", portalData.facing.toString()));
        portalData.topLeft = LegacyDataHandler.loadLocation(world, portalProperties[6]);
        portalData.gateFileName = portalProperties[7];
        portalData.destination = (portalProperties.length > 8) ? portalProperties[8] : "";
        String ownerString = (portalProperties.length > 10) ? portalProperties[10] : "";
        portalData.ownerUUID = LegacyDataHandler.getPlayerUUID(ownerString);
        portalData.flags = LegacyDataHandler.parseFlags(portalProperties);
        if (portalData.destination == null || portalData.destination.trim().isEmpty()) {
            portalData.flags.add(PortalFlag.NETWORKED);
        }

        if (portalProperties.length <= 9 || portalData.networkName.equalsIgnoreCase(defaultNetworkName)) {
            portalData.flags.add(PortalFlag.DEFAULT_NETWORK);
            portalData.networkName = LocalNetwork.DEFAULT_NETWORK_ID;
        } else if (!ownerString.isEmpty()) {
            String playerName = Bukkit.getOfflinePlayer(portalData.ownerUUID).getName();
            if (playerName != null && playerName.equals(portalData.networkName)) {
                portalData.flags.add(PortalFlag.PERSONAL_NETWORK);
                portalData.networkName = portalData.ownerUUID.toString();
            } else {
                portalData.flags.add(PortalFlag.CUSTOM_NETWORK);
            }
        } else {
            portalData.flags.add(PortalFlag.CUSTOM_NETWORK);
        }
        return portalData;
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
