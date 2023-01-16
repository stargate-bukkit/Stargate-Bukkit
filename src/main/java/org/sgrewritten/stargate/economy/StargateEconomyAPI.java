package org.sgrewritten.stargate.economy;

import org.bukkit.OfflinePlayer;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An API describing the economy methods required by Stargate
 */
public interface StargateEconomyAPI {

    /**
     * Sets up economy to make it ready for transactions
     */
    void setupEconomy();

    /**
     * Charges the given player for using the given portal
     *
     * @param player <p>The player to charge</p>
     * @param origin <p>The portal the player entered from, or null if not portal usage</p>
     * @param amount <p>The amount to charge the player</p>
     * @return <p>True if there were no problems in processing the payment</p>
     */
    boolean chargePlayer(OfflinePlayer player, Portal origin, int amount);

    /**
     * Refunds a player's payment for using a portal
     *
     * @param player <p>The player that used a portal</p>
     * @param origin <p>The portal the player used</p>
     * @param amount <p>The amount to refund the player</p>
     * @return <p>True if the player was successfully refunded</p>
     */
    boolean refundPlayer(OfflinePlayer player, Portal origin, int amount);

}
