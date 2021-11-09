package net.TheDgtl.Stargate;

import java.util.HashMap;

public enum Channel {
	GET_SERVER("GetServer"), PORTAL("SGPortal"), PLUGIN_ENABLE("SGPluginEnable"), PLUGIN_DISABLE("SGPluginDisable"),
	PLAYER_TELEPORT("SGTeleport"), FORWARD("Forward"), BUNGEE("BungeeCord"), PLAYER_CONNECT("Connect"),
	NETWORK_CHANGED("SGNetworkUpdate");

	private static final HashMap<String, Channel> map = new HashMap<>();
	static {
		for (Channel value : values()) {
			map.put(value.getChannel(), value);
		}
	}

	private final String channel;
	Channel(String channel){
		this.channel = channel;
	}
	
	public String getChannel() {
		return channel;
	}
	
	static public Channel parse(String channel) {
		return map.get(channel);
	}
}
