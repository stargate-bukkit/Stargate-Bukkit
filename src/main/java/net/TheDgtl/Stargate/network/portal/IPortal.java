package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public interface IPortal {


    void destroy();

    boolean isOpen();

    boolean isOpenFor(Entity target);

    void teleportHere(Entity target, Portal origin);

    void doTeleport(Entity target);

    void drawControlMechanism();

    void close(boolean force);

    public void onSignClick(Action action, Player player);

    void onButtonClick(PlayerInteractEvent event);

    void onIrisEntrance(Entity player);

    void open(Player player);

    String getName();

    void setNetwork(Network targetNet);

    void setOverrideDestination(IPortal destination);

    Network getNetwork();

    boolean hasFlag(PortalFlag flag);

    String getAllFlagsString();

    Location getSignPos();

    /**
     * Convert a portal into a string, would look like this:
     * Classname{key1=data1,key2=data2 ... }
     *
     * @param portal
     * @return
     */
    public static String getString(IPortal portal) {
        String type = portal.getClass().getName();

        HashMap<String, String> data = new HashMap<>();
        data.put("flags", portal.getAllFlagsString());
        data.put("name", portal.getName());
        data.put("net", portal.getNetwork().getName());
        if (Setting.getBoolean(Setting.USING_BUNGEE)) {
            data.put("server", Stargate.serverName);
        }
        String endMsg = type + "{";
        Iterator<String> it = data.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            endMsg = endMsg + key + "=" + data.get(key);
            if (it.hasNext())
                endMsg = endMsg + ",";
        }
        return endMsg + "}";
    }

    String getDesignName();

    UUID getOwnerUUID();
}
