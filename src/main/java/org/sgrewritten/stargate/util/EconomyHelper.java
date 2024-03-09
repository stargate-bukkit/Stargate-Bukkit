package org.sgrewritten.stargate.util;

import org.bukkit.entity.Player;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.config.ConfigurationHelper;

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
        if (portal instanceof RealPortal realPortal) {
            Portal destination = realPortal.getBehavior().getDestination();
            if (destination == null) {
                return false;
            }
            return ConfigurationHelper.getBoolean(ConfigurationOption.CHARGE_FREE_DESTINATION) ||
                    !portal.hasFlag(StargateFlag.FIXED) ||
                    !destination.hasFlag(StargateFlag.FREE);
        }
        return false;
    }

}
