package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.property.BypassPermission;
import org.bukkit.entity.Player;

/**
 * A helper class for dealing with economy
 */
public final class EconomyHelper {

    private EconomyHelper() {

    }

    /**
     * Checks whether the given player should be charged for destroying a portal
     *
     * @param player           <p>The player to check</p>
     * @param portal           <p>The portal the player is trying to do something with</p>
     * @param bypassPermission <p>The bypass permission that would let the player avoid payment</p>
     * @return <p>True if the player should be charged</p>
     */
    public static boolean shouldChargePlayer(Player player, Portal portal, BypassPermission bypassPermission) {
        if (player.hasPermission(bypassPermission.getPermissionString())) {
            return false;
        }

        return ConfigurationHelper.getBoolean(ConfigurationOption.CHARGE_FREE_DESTINATION) ||
                !portal.hasFlag(PortalFlag.FIXED) ||
                !portal.getDestination().hasFlag(PortalFlag.FREE);
    }

}
