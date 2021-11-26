package net.TheDgtl.Stargate;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.TheDgtl.Stargate.network.portal.IPortal;
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
	public boolean chargePlayer(OfflinePlayer player, int amount) {
		Stargate.log(Level.FINE, "Charging player " + amount);
		if(!hasVault)
			return true;
		
		EconomyResponse response = econ.withdrawPlayer(player, amount);
		return response.transactionSuccess();
	}
	
	public boolean depositPlayer(OfflinePlayer player, int amount) {
		Stargate.log(Level.FINE, "Depositing player " + amount);
		if(!hasVault)
			return true;
		EconomyResponse response = econ.depositPlayer(player, amount);
		if(!response.transactionSuccess()) {
			econ.createPlayerAccount(player);
			return response.transactionSuccess();
		}
		return true;
	}
	
	/**
	 * Money for the taxgods. May you live in wealth
	 * @param player
	 * @param amount
	 * @return
	 */
	public boolean chargeAndTax(OfflinePlayer player, int amount) {
		String bankUUIDStr = Setting.getString(Setting.TAX_DESTINATION);
		if(!bankUUIDStr.isEmpty()) {
			OfflinePlayer bankAccount = Bukkit.getOfflinePlayer(UUID.fromString(bankUUIDStr));
			return chargeAndDepocitPlayer(player,bankAccount,amount);
		}
		return chargePlayer(player,amount);
	}
	
	public boolean chargePlayer(OfflinePlayer player, OfflinePlayer transactionTarget, int amount) {
		if(Setting.getBoolean(Setting.GATE_OWNER_REVENUE))
			return chargeAndDepocitPlayer(player,transactionTarget,amount);
		return chargeAndTax(player,amount);
	}
	
	private boolean chargeAndDepocitPlayer(OfflinePlayer player, OfflinePlayer transactionTarget, int amount) {
		if(chargePlayer(player, amount)) {
			return depositPlayer(transactionTarget, amount);
		}
		return false;
	}
}
