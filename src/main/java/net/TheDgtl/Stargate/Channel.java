package net.TheDgtl.Stargate;

import java.util.HashMap;
import java.util.Map;

/**
 * The channel represents the different usable plugin channels that can be used with BungeeCord
 */
public enum Channel {

    /**
     * Represents the BungeeCord plugin channel
     */
    BUNGEE("BungeeCord"),

    /**
     * Represents the BungeeCord sub-channel for getting the name of this server
     */
    GET_SERVER("GetServer"),

    /**
     * Represents the BungeeCord sub-channel for making a player connect to a server
     */
    PLAYER_CONNECT("Connect"),

    /**
     * Represents the BungeeCord sub-channel for forwarding a custom plugin message to another server
     */
    FORWARD("Forward"),

    /**
     * Represents the custom forwarding sub-channel used when a player is teleported from a stargate through BungeeCord
     */
    PLAYER_TELEPORT("SGTeleport"),

    /**
     * Represents the custom forwarding sub-channel used when a stargate is added to or removed from a network
     */
    NETWORK_CHANGED("SGNetworkUpdate"),

    /**
     * Represents the custom forwarding sub-channel used by legacy stargate plugins; for backwards compatibility
     */
    LEGACY_BUNGEE("SGBungee"),

    /**
     * Represents the custom forwarding sub-channel used for nothing ???
     * 
     * TODO: Figure out why this is never used or mentioned
     */
    PORTAL("SGPortal"),

    /**
     * Represents the custom forwarding sub-channel used for nothing ???
     */
    PLUGIN_ENABLE("SGPluginEnable"),

    /**
     * Represents the custom forwarding sub-channel used for nothing ???
     */
    PLUGIN_DISABLE("SGPluginDisable");

    private static final Map<String, Channel> map = new HashMap<>();

    private final String channel;

    /**
     * Instantiates a new plugin channel
     * 
     * @param channel <p>The name/string representation of the new plugin channel</p>
     */
    Channel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets this plugin channel's name/string representation
     * 
     * @return <p>This plugin channel's string representation</p>
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Parses a channel's string representation into a channel enum
     * 
     * @param channel <p>The channel name to parse</p>
     * @return <p>The matching channel enum, or null if no match was found</p>
     */
    static public Channel parse(String channel) {
        if (map.isEmpty()) {
            for (Channel value : values()) {
                map.put(value.getChannel(), value);
            }
        }
        
        return map.get(channel);
    }
    
}
