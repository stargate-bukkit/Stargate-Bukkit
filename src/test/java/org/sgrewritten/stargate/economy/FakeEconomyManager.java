package org.sgrewritten.stargate.economy;

import org.bukkit.OfflinePlayer;
import org.sgrewritten.stargate.network.portal.Portal;

public class FakeEconomyManager implements StargateEconomyAPI {

    @Override
    public void setupEconomy() {

    }

    @Override
    public boolean chargePlayer(OfflinePlayer player, Portal origin, int amount) {
        return true;
    }

    @Override
    public boolean refundPlayer(OfflinePlayer player, Portal origin, int amount) {
        return true;
    }
    
    
    
}
