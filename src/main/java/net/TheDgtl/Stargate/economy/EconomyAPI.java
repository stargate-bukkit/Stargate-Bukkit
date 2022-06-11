package net.TheDgtl.Stargate.economy;

import org.bukkit.OfflinePlayer;

/**
 * An API describing the necessary basic instructions necessary for economy
 */
public interface EconomyAPI {

    /**
     * Check if player has enough money
     *
     * @param target <p>The player to check</p>
     * @param amount <p>The amount required</p>
     * @return <p>True if the player has the required amount</p>
     */
    boolean has(OfflinePlayer target, int amount);

    /**
     * Charges a player if possible
     *
     * @param offlinePlayer <p>The player to charge</p>
     * @param amount        <p>The amount the player should be charged</p>
     * @return <p>True if the payment was fulfilled</p>
     */
    boolean chargePlayer(OfflinePlayer offlinePlayer, int amount);

    /**
     * Deposits money in a player's account if possible
     *
     * @param player <p>The player receiving the money</p>
     * @param amount <p>The amount to receive</p>
     * @return <p>True if the payment was fulfilled</p>
     */
    boolean depositPlayer(OfflinePlayer player, int amount);

}
