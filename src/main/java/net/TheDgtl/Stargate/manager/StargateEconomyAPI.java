package net.TheDgtl.Stargate.manager;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.OfflinePlayer;

/**
 * An API describing the economy methods required by Stargate
 */
public interface StargateEconomyAPI extends EconomyAPI {

    /**
     * Sets up economy to make it ready for transactions
     */
    void setupEconomy();

    /**
     * Charges the given player
     *
     * @param player <p>The player to charge</p>
     * @param origin <p>The portal the player entered from</p>
     * @param amount <p>The amount to charge the player</p>
     * @return <p>True if there were no problems in processing the payment</p>
     */
    boolean chargePlayer(OfflinePlayer player, Portal origin, int amount);

    /**
     * Money for the tax gods. May you live in wealth
     *
     * @param player <p>The player to charge and tax</p>
     * @param amount <p>The amount the player should be charged</p>
     * @return <p>True if the player was charged and/or taxed</p>
     */
    boolean chargeAndTax(OfflinePlayer player, int amount);

}
