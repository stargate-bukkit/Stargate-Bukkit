package net.TheDgtl.Stargate.network.portal;


import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * An interface describing any portal
 */
public interface Portal {

    /**
     * The method used when destroying a portal
     *
     * <p>This should remove all references to the portal, both from temporary memory and from any databases.</p>
     */
    void destroy();

    /**
     * Checks whether this portal is currently open
     *
     * @return <p>True if this portal is currently open</p>
     */
    boolean isOpen();

    /**
     * Checks whether this portal is currently open and the given target is the one the portal opened for
     *
     * @param target <p>The target to check</p>
     * @return <p>True if the portal has been opened for the given target</p>
     */
    boolean isOpenFor(Entity target);

    /**
     * Teleports an entity to this portal
     *
     * @param target <p>The target entity to teleport</p>
     * @param origin <p>The origin portal the entity is teleporting from</p>
     */
    void teleportHere(Entity target, RealPortal origin);

    /**
     * Teleports the given entity to this portal's current destination
     *
     * @param target <p>The entity to teleport</p>
     */
    void doTeleport(Entity target);

    /**
     * Closes this portal
     *
     * @param forceClose <p>Whether to force this portal to close, even if set to always on or similar</p>
     */
    void close(boolean forceClose);

    /**
     * Opens this portal for the given player
     *
     * @param player <p>The player to open this portal for</p>
     */
    void open(Player player);

    /**
     * Gets the name of this portal
     *
     * @return <p>The name of this portal</p>
     */
    String getName();

    /**
     * Forces this portal to temporarily go to the given destination regardless of the normal destination(s)
     *
     * @param destination <p>The destination this portal should temporarily connect ot</p>
     */
    void overrideDestination(Portal destination);

    /**
     * Gets the network this portal belongs to
     *
     * @return <p>The network this portal belongs to</p>
     */
    Network getNetwork();

    /**
     * Changes the network this portal belongs to
     *
     * @param targetNetwork <p>The new network this portal should belong to</p>
     */
    void setNetwork(Network targetNetwork);

    /**
     * Checks whether this portal has the given portal flag enabled
     *
     * @param flag <p>The portal flag to check for</p>
     * @return <p>True if this portal has the given portal flag enabled</p>
     */
    boolean hasFlag(PortalFlag flag);

    /**
     * Gets all of this portal's portal flags in the form of a string
     *
     * <p>This returns the concatenation of all character representations for the flags used by this portal.</p>
     *
     * @return <p>All of this portal's portal flags in the form of a string</p>
     */
    String getAllFlagsString();

    /**
     * Gets the location of this portal's sign
     *
     * @return <p>The location of this portal's sign</p>
     */
    Location getSignLocation();

    /**
     * Gets the name of this portal's used gate design
     *
     * @return <p>The name of this portal's used gate design</p>
     */
    String getDesignName();

    /**
     * Gets the UUID of this portal's owner
     *
     * <p>A portal's owner is the player that created the portal.</p>
     *
     * @return <p>The UUID of this portal's owner</p>
     */
    UUID getOwnerUUID();

    /**
     * Looks into available portals to connect to, and updates appearance and behaviour accordingly
     */
    void update();

    /**
     * Gets a string representation of the given portal
     *
     * <p>Convert a portal into a string, would look like this: Classname{key1=data1,key2=data2 ... }</p>
     *
     * @param portal <p>The portal to convert to a string</p>
     * @return <p>A string representing the given portal</p>
     */
    static String getString(Portal portal) {
        String type = portal.getClass().getName();

        HashMap<String, String> data = new HashMap<>();
        data.put("flags", portal.getAllFlagsString());
        data.put("name", portal.getName());
        data.put("net", portal.getNetwork().getName());
        if (Settings.getBoolean(Setting.USING_BUNGEE)) {
            data.put("server", Stargate.serverName);
        }
        StringBuilder endMsg = new StringBuilder(type + "{");
        Iterator<String> it = data.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            endMsg.append(key).append("=").append(data.get(key));
            if (it.hasNext())
                endMsg.append(",");
        }
        return endMsg + "}";
    }

}
