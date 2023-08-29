package org.sgrewritten.stargate.util;

import org.bukkit.entity.Player;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.property.BypassPermission;

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
