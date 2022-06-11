package net.TheDgtl.Stargate.economy;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * An API describing the economy methods required by Stargate
 */
public interface StargateEconomyAPI extends EconomyAPI {

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
     * Gets the receiver of a transaction for using the given portal
     *
     * @param player <p>The player using the portal</p>
     * @param origin <p>The portal used</p>
     * @return <p>The target account's UUID, or null for no receiver</p>
     */
    UUID getTransactionReceiver(OfflinePlayer player, Portal origin);

}
