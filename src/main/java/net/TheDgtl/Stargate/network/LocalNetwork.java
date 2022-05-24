package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLQueryGenerator;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.NameHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A network of portals
 */
public class LocalNetwork implements Network {

    protected Map<String, Portal> nameToPortalMap;
    protected Database database;
    protected String name;
    protected SQLQueryGenerator sqlQueryGenerator;
    private RegistryAPI registry;

    /**
     * Instantiates a new network
     *
     * @param name           <p>The name of the new network</p>
     * @param database       <p>The database to use for saving network data</p>
     * @param queryGenerator <p>The generator to use for generating SQL queries</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public LocalNetwork(String name, Database database, SQLQueryGenerator queryGenerator) throws NameErrorException {
        if (name.trim().isEmpty() || (name.length() >= Stargate.MAX_TEXT_LENGTH)) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }
        this.name = name.trim();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            this.name = ChatColor.stripColor(this.name);
        }
        this.database = database;
        this.sqlQueryGenerator = queryGenerator;
        nameToPortalMap = new HashMap<>();
    }

    @Override
    public String getId() {
        return NameHelper.getID(this.name);
    }

    @Override
    public Collection<Portal> getAllPortals() {
        return nameToPortalMap.values();
    }

    @Override
    public Portal getPortal(String name) {
        if (name == null) {
            return null;
        }
        return nameToPortalMap.get(NameHelper.getID(name));
    }

    @Override
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        nameToPortalMap.remove(portal.getID());
        if (!removeFromDatabase) {
            return;
        }
        Stargate.getRegistry().removePortal(portal, PortalType.LOCAL);
    }

    @Override
    public void addPortal(Portal portal, boolean saveToDatabase) throws NameErrorException {
        if (isPortalNameTaken(portal.getName())) {
            throw new NameErrorException(TranslatableMessage.ALREADY_EXIST);
        }
        if (portal instanceof RealPortal) {
            RealPortal realPortal = (RealPortal) portal;
            for (GateStructureType key : GateStructureType.values()) {
                List<BlockLocation> locations = realPortal.getGate().getLocations(key);
                if (locations == null) {
                    continue;
                }
                registry.registerLocations(key, generateLocationMap(locations, (RealPortal) portal));
            }
            if (saveToDatabase) {
                savePortal((RealPortal) portal);
            }
        }
        nameToPortalMap.put(portal.getID(), portal);
    }

    @Override
    public boolean isPortalNameTaken(String name) {
        return nameToPortalMap.containsKey(NameHelper.getID(name));
    }

    @Override
    public void updatePortals() {
        for (String portal : nameToPortalMap.keySet()) {
            getPortal(portal).updateState();
        }
    }

    @Override
    public Set<String> getAvailablePortals(Player player, Portal requester) {
        Set<String> tempPortalList = new HashSet<>(nameToPortalMap.keySet());
        tempPortalList.remove(requester.getID());
        if (!requester.hasFlag(PortalFlag.FORCE_SHOW)) {
            Set<String> removeList = new HashSet<>();
            for (String portalName : tempPortalList) {
                Portal target = getPortal(portalName);
                if (target.hasFlag(PortalFlag.HIDDEN) &&
                        (player != null && !player.hasPermission(BypassPermission.HIDDEN.getPermissionString()))) {
                    removeList.add(portalName);
                }
                if (target.hasFlag(PortalFlag.PRIVATE) && player != null &&
                        !player.hasPermission(BypassPermission.PRIVATE.getPermissionString()) &&
                        !player.getUniqueId().equals(target.getOwnerUUID())) {
                    removeList.add(portalName);
                }
            }
            tempPortalList.removeAll(removeList);
        }
        return tempPortalList;
    }

    @Override
    public String getHighlightedName() {
        return HighlightingStyle.NETWORK.getHighlightedName(getName());
    }

    @Override
    public void destroy() {
        for (String portalName : nameToPortalMap.keySet()) {
            Portal portal = nameToPortalMap.get(portalName);
            portal.destroy();
        }
        nameToPortalMap.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int size() {
        return this.getAllPortals().size();
    }

    /**
     * Saves the given portal to the database
     *
     * @param portal <p>The portal to save</p>
     */
    protected void savePortal(RealPortal portal) {
        registry.savePortal(portal, PortalType.LOCAL);
    }

    /**
     * Gets a map between the given block locations and the given portal
     *
     * @param locations <p>The locations related to the portal</p>
     * @param portal    <p>The portal with blocks at the given locations</p>
     * @return <p>The resulting location to portal mapping</p>
     */
    private Map<BlockLocation, RealPortal> generateLocationMap(List<BlockLocation> locations, RealPortal portal) {
        Map<BlockLocation, RealPortal> output = new HashMap<>();
        for (BlockLocation location : locations) {
            output.put(location, portal);
        }
        return output;
    }

    @Override
    public void assignToRegistry(RegistryAPI registry) {
        this.registry = registry;
    }

}
