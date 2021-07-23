package net.TheDgtl.Stargate.portal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;

public class InterserverPortal extends Portal{

	private String targetServer;

	InterserverPortal(Network network, Block sign, String[] lines, String targetServer) throws NameError, NoFormatFound, GateConflict {
		super(network, sign, lines);
		this.targetServer = targetServer;
	}

	@Override
	public void onSignClick(Action action, Player actor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawControll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Portal loadDestination() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Legacy bungee implementation
	 */
	@Override
	public void doTeleport(Player player) {
		// Teleport the player back to this gate, for sanity's sake
		player.teleport(getExit());
		
		// Send the SGBungee packet first, it will be queued by BC if required
		Stargate stargate = Stargate.getPlugin(Stargate.class);
		try {
			// Build the message, format is <player>#@#<destination>
            String msg = player.getName() + "#@#" + destination.name;
            // Build the message data, sent over the SGBungee bungeecord channel
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF("Forward");
            msgData.writeUTF(targetServer);    	// Server
            msgData.writeUTF("SGBungee");      	// Channel
            msgData.writeShort(msg.length());   // Data Length
            msgData.writeBytes(msg);            // Data
            player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
		} catch (IOException ex) {
            stargate.getLogger().severe("Error sending BungeeCord teleport packet");
            ex.printStackTrace();
            return;
        }
		
		 // Connect player to new server
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF("Connect");
            msgData.writeUTF(network.name);

            player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
            bao.reset();
        } catch (IOException ex) {
            stargate.getLogger().severe("[Stargate] Error sending BungeeCord connect packet");
            ex.printStackTrace();
            return;
        }
	}
}
