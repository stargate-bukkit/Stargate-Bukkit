package net.TheDgtl.Stargate.refactoring.retcons;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.PlaceholderPortal;
import net.TheDgtl.Stargate.network.portal.Portal;

import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.util.FileHelper;
import net.TheDgtl.Stargate.util.PortalCreationHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LegacyPortalStorageLoader {

    private final static Map<PortalFlag, Integer> LEGACY_FLAGS_POS_MAP = new EnumMap<>(PortalFlag.class);

    static {
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.HIDDEN, 11);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.ALWAYS_ON, 12);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.PRIVATE, 13);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.FREE, 15);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.BACKWARDS, 16);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.FORCE_SHOW, 17);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.HIDE_NETWORK, 18);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.RANDOM, 19);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.BUNGEE, 20);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.SILENT, 21);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.NO_SIGN, 22);
    }

    /**
     * @param portalSaveLocation <p> Filename</p>
     * @return
     * @throws IOException
     */
    static public List<Portal> loadPortalsFromStorage(String portalSaveLocation, Server server, StargateFactory factory) throws IOException {
        List<Portal> portals = new ArrayList<>();
        File dir = new File(portalSaveLocation);
        System.out.println(portalSaveLocation);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".db")) : new File[0];
        for (File file : files) {
            String worldName = file.getName().replaceAll("\\.db$", "");
            BufferedReader reader = FileHelper.getBufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                    try {
                        portals.add(readPortal(line, server.getWorld(worldName),factory));
                    } catch (NameErrorException | NoFormatFoundException | GateConflictException e) {
                        e.printStackTrace();
                    }
                
                line = reader.readLine();
            }
        }
        return portals;
    }

    static private Portal readPortal(String line, World world, StargateFactory factory) throws NameErrorException, NoFormatFoundException, GateConflictException {
        String[] splitLine = line.split(":");
        String name = splitLine[0];
        String[] coordinates = splitLine[1].split(",");
        Location signLocation = new Location(
                world,
                Double.parseDouble(coordinates[0]),
                Double.parseDouble(coordinates[1]),
                Double.parseDouble(coordinates[2]));
        String gateFormatName = splitLine[7];
        String destination = (splitLine.length > 8) ? splitLine[8] : "";
        String networkName = (splitLine.length > 9) ? splitLine[9] : Settings.getString(Setting.DEFAULT_NET);
        String ownerString = (splitLine.length > 10) ? splitLine[10] : "";
        UUID ownerUUID = getPlayerUUID(ownerString);
        EnumSet<PortalFlag> flags = parseFlags(splitLine);
        if(destination == null || destination.trim().isEmpty())
            flags.add(PortalFlag.NETWORKED);
        try {
            factory.createNetwork(networkName, flags);
        } catch (NameErrorException ignored) {
        }
        Network network = factory.getNetwork(networkName, flags.contains(PortalFlag.FANCY_INTER_SERVER));
        Portal portal = new PlaceholderPortal(name,network,destination,flags,signLocation,gateFormatName,ownerUUID);
        network.addPortal(portal, true);

        return portal;
    }

    @SuppressWarnings("deprecation")
    private static UUID getPlayerUUID(String ownerString) {
        if (ownerString.length() > 16)
            return UUID.fromString(ownerString);
        return Bukkit.getOfflinePlayer(ownerString).getUniqueId();
    }

    private static EnumSet<PortalFlag> parseFlags(String[] splitedLine) {
        EnumSet<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);
        for (PortalFlag flag : LEGACY_FLAGS_POS_MAP.keySet()) {
            int position = LEGACY_FLAGS_POS_MAP.get(flag);
            if (splitedLine.length > position && splitedLine[position].equalsIgnoreCase("true")) {
                flags.add(flag);
            }
        }

        return flags;
    }

}
