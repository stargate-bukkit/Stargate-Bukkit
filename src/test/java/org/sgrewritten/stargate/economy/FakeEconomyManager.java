package org.sgrewritten.stargate.economy;

import org.bukkit.OfflinePlayer;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class FakeEconomyManager implements StargateEconomyAPI {

    boolean triggeredSetup;
    
    @Override
    public void setupEconomy() {
        this.triggeredSetup = true;
    }

    @Override
    public boolean chargePlayer(OfflinePlayer player, Portal origin, int amount) {
        return true;
    }

    @Override
    public boolean refundPlayer(OfflinePlayer player, Portal origin, int amount) {
        return true;
    }
    
    public boolean hasTriggeredSetupEconomy() {
        return this.triggeredSetup;
        
    }
    
}
