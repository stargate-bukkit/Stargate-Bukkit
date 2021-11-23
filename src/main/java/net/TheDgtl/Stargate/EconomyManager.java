package net.TheDgtl.Stargate;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyManager {

	
	private Economy econ;
	private boolean hasVault;
	
	public EconomyManager() {
		if(!Setting.getBoolean(Setting.USE_ECONOMY)){
			hasVault = false;
			return;
		}
		hasVault = setupEconomy();
		// TODO wording, or maybe its fine?
		if(!hasVault)
			Stargate.log(Level.WARNING, "Economy fucked up");
	}
	
	private boolean setupEconomy() {
		if(Bukkit.getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
	}
	
	/**
	 * 
	 * @param player
	 * @param amount
	 * @return if player had enough money for transaction
	 */
	public boolean chargePlayer(Player player, int amount) {
		Stargate.log(Level.FINE, "Charging player " + amount);
		if(!hasVault)
			return true;
		
		EconomyResponse response = econ.withdrawPlayer(player, amount);
		return response.transactionSuccess();
	}
}
