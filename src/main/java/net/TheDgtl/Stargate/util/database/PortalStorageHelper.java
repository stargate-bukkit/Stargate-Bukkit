package net.TheDgtl.Stargate.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.portal.PortalData;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.LegacyDataHandler;

public class PortalStorageHelper {


    public static PortalData loadPortalData(ResultSet resultSet, PortalType portalType) throws SQLException {
        PortalData portalData = new PortalData();
        portalData.name = resultSet.getString("name");
        portalData.networkName = resultSet.getString("network");
        portalData.destination = resultSet.getString("destination");
        // Make sure to treat no destination as empty, not a null string
        if (resultSet.wasNull()) {
            portalData.destination = "";
        }

        World world = Bukkit.getWorld( resultSet.getString("world"));
        if (world == null) {
            return null;
        }
        portalData.topLeft = new Location(world, resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
        
        portalData.flagString = resultSet.getString("flags");
        portalData.flags =  PortalFlag.parseFlags(portalData.flagString);
        portalData.ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
        portalData.gateFileName = resultSet.getString("gateFileName");
        portalData.flipZ = resultSet.getBoolean("flipZ");
        portalData.facing = getBlockFaceFromOrdinal(Integer.parseInt(resultSet.getString("facing")));
        portalData.portalType = portalType;
        //TODO Check if portalType is necessary to keep track of // there's already flags.contains(PortalFlag.FANCY_INTERSERVER)
        if (portalType == PortalType.INTER_SERVER) {
            portalData.serverUUID = resultSet.getString("homeServerId");
            Stargate.log(Level.FINEST, "serverUUID = " + portalData.serverUUID);
            if (!portalData.serverUUID.equals(Stargate.getServerUUID())) {
                portalData.serverName = resultSet.getString("serverName");
            }
        }
        
        return portalData;
    }
    
    /**
     * Load legacy data
     * @param portalProperties <p> A data sequence in the format of legacy storage resembling a portal</p>
     * @return
     */
    public static PortalData loadPortalData(String[] portalProperties, World world) {
        PortalData portalData = new PortalData();
        portalData.name = portalProperties[0];
        portalData.networkName = (portalProperties.length > 9) ? portalProperties[9] : ConfigurationHelper.getString(
                ConfigurationOption.DEFAULT_NETWORK);

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
