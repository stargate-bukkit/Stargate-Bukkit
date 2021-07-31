package net.TheDgtl.Stargate;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.TheDgtl.Stargate.network.InterserverNetwork;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;

public class StargateData {
	static final String ADD_PORTAL = "add";
	static final String REMOVE_PORTAL = "remove";
	static final String TARGET = "target";
	static final String SERVER = "server";
	
	static public abstract class StargateDataSender{
		protected String target;
		
		public StargateDataSender(String targetServer) {
			this.target = targetServer;
		}
		
		protected abstract JsonObject getJson();
		
		protected abstract Channel getChannel();

		/**
		 * Send the data specified in getJson() to target server. This is how different
		 * stargate instances send data to eachother through bungee server network
		 */
		public void send() {
			String jsonString = getJson().toString();
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			//bukkit protocol for just send data and do nothing else to
			out.writeUTF(Channel.FORWARD.getChannel()); 
			// this server ("All" is every server)
			out.writeUTF(target); 
			
			//Stargate protocol (this is the data that is going to be sent to the other stargate instance)
			// Specify a channel so that message will be easier to process
			out.writeUTF(getChannel().getChannel());
			// add all data
			out.writeUTF(jsonString);
			// send everything
			Bukkit.getServer().sendPluginMessage(Stargate.getPlugin(Stargate.class), Channel.BUNGEE.getChannel(), out.toByteArray());
		
		}
	}
	
	static public class PortalDataSender extends StargateDataSender{
		JsonObject obj;
		
		public PortalDataSender(String targetServer) {
			super(targetServer);
			obj = new JsonObject();
			obj.add(SERVER, new JsonPrimitive(Stargate.serverName));
		} 
		
		/**
		 * Add a portal which will be sent to be registered in other stargate instances
		 * @param portal
		 */
		public void addPortal(IPortal portal) {
			JsonPrimitive item = new JsonPrimitive(IPortal.getString(portal));
			addToList(ADD_PORTAL,item);
		}
		
		/**
		 * Add a portal which will be sent to be unregistered in other stargate instances
		 * @param portal
		 */
		public void destroyPortal(IPortal portal) {
			JsonPrimitive item = new JsonPrimitive(IPortal.getString(portal));
			addToList(REMOVE_PORTAL,item);
		}
		
		/**
		 * Reduce code duplication
		 * @param listKey
		 * @param item
		 */
		private void addToList(String listKey, JsonElement item) {
			if (!obj.has(listKey)) {
				obj.add(listKey, new JsonArray());
			}
			obj.get(listKey).getAsJsonArray().add(item);
		}


		@Override
		protected JsonObject getJson() {
			return obj;
		}


		@Override
		protected Channel getChannel() {
			return Channel.PORTAL;
		}
		
		
		
	}

	static public class PlayerDataSender extends StargateDataSender{
		JsonObject obj;
		Player player;
		public PlayerDataSender(String targetServer, String targetPortal, Player player) {
			super(targetServer);
			this.player = player;
			JsonObject obj = new JsonObject();
			obj.addProperty(SERVER, Stargate.serverName);
			obj.addProperty(player.getName(), targetServer);
			obj.addProperty(TARGET, targetPortal);
		}
		
		@Override
		protected JsonObject getJson() {
			return obj;
		}

		@Override
		protected Channel getChannel() {
			return Channel.PLAYER_TELEPORT;
		}
		
		@Override
		public void send() {
			super.send();
			// Send player change server message (not stargate protocol, bungee protocol)
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(Channel.PLAYER_CONNECT.getChannel());
			out.writeUTF(target);
			player.sendPluginMessage(Stargate.getPlugin(Stargate.class), Channel.BUNGEE.getChannel(),
					out.toByteArray());
		}
	}
	
	static public abstract class StargateDataReciever {
		protected JsonObject obj;
		StargateDataReciever(String jsonData){
			JsonParser parser = new JsonParser();
			obj = (JsonObject) parser.parse(jsonData);
		}
		
		public abstract void doResponse();
	}
	
	static public class PotalDataReceiver extends StargateDataReciever{

		public PotalDataReceiver(String jsonData) {
			super(jsonData);
		}
		
		/**
		 * TODO come up with a good script to read this kind of data
		 */
		@Override
		public void doResponse() {
			String server = obj.getAsJsonPrimitive(SERVER).getAsString();
			JsonArray add_portals = obj.get(ADD_PORTAL).getAsJsonArray();
			Iterator<JsonElement> it= add_portals.iterator();
			while(it.hasNext()) {
				String portalStr = it.next().toString();
				try {
					IPortal.createFromString(portalStr, true);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			JsonArray rmv_portals = obj.get(REMOVE_PORTAL).getAsJsonArray();
			Iterator<JsonElement> rm_it = rmv_portals.iterator();
			while (rm_it.hasNext()) {
				String portalStr = rm_it.next().toString();
				String dataStr = portalStr.substring(portalStr.indexOf("{") + 1, portalStr.indexOf("}"));
				String[] data = portalStr.split(",");
				String net = "";
				String name = "";
				for (String dataItem : data) {
					String[] temp = dataItem.split("=");
					String key = temp[0];
					String value = temp[1];
					switch (key) {
					case "net":
						net = value;
						break;
					case "name":
						name = value;
					}
				}
				InterserverNetwork network = InterserverNetwork.getNetwork(net, false);
				network.removePortal(name);
			}
		}
	}
}
