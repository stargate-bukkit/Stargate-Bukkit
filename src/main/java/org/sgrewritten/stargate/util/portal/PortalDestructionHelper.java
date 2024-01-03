package org.sgrewritten.stargate.util.portal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateDestroyPortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.util.EconomyHelper;
import org.sgrewritten.stargate.util.MessageUtils;

/**
 * A helper class for removing an existing portal
 */
public final class PortalDestructionHelper {

    private PortalDestructionHelper() {

    }

    /**
     * Destroys a portal if the entity has permission and can pay any fees
     *
     * @param player        <p>The player that initiated the destruction</p>
     * @param portal        <p>The portal to be destroyed</p>
     * @param destroyAction <p>The action to run if the destruction is performed</p>
     * @return <p>True if the destruction has been cancelled</p>
     */
    public static boolean destroyPortalIfHasPermissionAndCanPay(Player player, Portal portal, Runnable destroyAction, LanguageManager languageManager, StargateEconomyAPI economyManager) {
        double cost = ConfigurationHelper.getDouble(ConfigurationOption.DESTROY_COST);
        StargatePermissionManager permissionManager = new StargatePermissionManager(player, languageManager);

        boolean hasPermission = permissionManager.hasDestroyPermissions((RealPortal) portal);
        StargateDestroyPortalEvent portalDestroyEvent = new StargateDestroyPortalEvent(portal, player, !hasPermission,
                permissionManager.getDenyMessage(), cost);
        Bukkit.getPluginManager().callEvent(portalDestroyEvent);

        // Inform the player why the destruction was denied
        if (portalDestroyEvent.getDeny()) {
            String message = null;
            if (portalDestroyEvent.getDenyReason() == null) {
                message = languageManager.getErrorMessage(TranslatableMessage.ADDON_INTERFERE);
            } else if (!portalDestroyEvent.getDenyReason().isEmpty()) {
                message = portalDestroyEvent.getDenyReason();
            }
            MessageUtils.sendMessageFromPortal(portal, player, message, MessageType.DENY);
            return true;
        }

        /*
         * If setting charge free destination is false, destination portal is
         * PortalFlag.Free and portal is of Fixed type or if player has override cost
         * permission, do not collect money
         */
        if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_DESTROY)
                && !economyManager.chargePlayer(player, null, portalDestroyEvent.getCost())) {
            String message = languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS);
            MessageUtils.sendMessageFromPortal(portal, player, message, MessageType.DENY);
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Stargate.getInstance(), destroyAction);
        return false;
    }

}
