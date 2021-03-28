/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.TheDgtl.Stargate;

import java.util.Objects;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EconomyHandler {
	
	private Server server;
	private boolean economyEnabled = false;
	private Economy economy = null;
	private Plugin vault = null;

	private int useCost = 0;
	private int createCost = 0;
	private int destroyCost = 0;
	private boolean toOwner = false;
	private boolean chargeFreeDestination = true;
	private boolean freeGatesGreen = false;

	public EconomyHandler(Server server, FileConfiguration config) {
        this.server = server;
        this.economyEnabled = config.getBoolean("useeconomy");
        this.createCost = config.getInt("createcost");
        this.destroyCost = config.getInt("destroycost");
        this.useCost = config.getInt("usecost");
        this.toOwner = config.getBoolean("toowner");
        this.chargeFreeDestination = config.getBoolean("chargefreedestination");
        this.freeGatesGreen = config.getBoolean("freegatesgreen");
        


        if (!economyEnabled) {
        	vault = null;
        	economy = null;
        }
	}
	public double getBalance(@NotNull Player player) {
        return !economyEnabled ? 0 : economy.getBalance(player);
    }

    @Deprecated
    public boolean chargePlayer(@NotNull Player player, @NotNull String target, double amount) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(target);
        if (!economyEnabled || player.getName().equals(target)) return true;
        if (economy == null || !economy.has(player, amount)) return false;

        economy.withdrawPlayer(player, amount);
        economy.depositPlayer(target, amount);

        return true;
    }

    public boolean chargePlayer(@NotNull Player player, @NotNull UUID target, double amount) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(target);
        if (!economyEnabled || player.getUniqueId().compareTo(target) == 0) return true;
        if (economy == null || !economy.has(player, amount)) return false;

        economy.withdrawPlayer(player, amount);
        economy.depositPlayer(Bukkit.getOfflinePlayer(target), amount);

        return true;
    }

    public boolean chargePlayer(@NotNull Player player, double amount) {
        Objects.requireNonNull(player);
        if (!economyEnabled) return true;
        if (economy == null || !economy.has(player, amount)) return false;

        economy.withdrawPlayer(player, amount);
        return true;
    }

    public String format(int amt) {
        return economyEnabled ? economy.format(amt) : "";
    }

    public boolean setupEconomy(@NotNull PluginManager pm) {
        if (!economyEnabled) return false;


        // Check for Vault
        Plugin p = Objects.requireNonNull(pm).getPlugin("Vault");
        if (p != null && p.isEnabled()) {
            RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                vault = p;

                return true;
            }
        }

        economyEnabled = false;
        return false;
    }

    public boolean useEconomy() {
        return economyEnabled && economy != null;
    }

    public boolean isEconomyEnabled() { return economyEnabled;}
    @Nullable
    public Economy getEconomy() {return economy;}
    @Nullable
    public Plugin getVault() {return vault;}

    public int getUseCost() {return useCost;}

    public int getCreateCost() { return createCost;}

    public int getDestroyCost() {return destroyCost;}

    public boolean isToOwner() {return toOwner; }

    public boolean isChargeFreeDestination() {return chargeFreeDestination;}

    public boolean isFreeGatesGreen() {return freeGatesGreen;}
    
}
