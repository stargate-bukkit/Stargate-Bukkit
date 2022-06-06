package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.action.SupplierAction;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.manager.StargatePermissionManager;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.EconomyHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

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
    public static boolean destroyPortalIfHasPermissionAndCanPay(Player player, Portal portal, Runnable destroyAction) {
        int cost = ConfigurationHelper.getInteger(ConfigurationOption.DESTROY_COST);
        StargatePermissionManager permissionManager = new StargatePermissionManager(player);

        boolean hasPermission = permissionManager.hasDestroyPermissions((RealPortal) portal);
        StargateDestroyEvent stargateDestroyEvent = new StargateDestroyEvent(portal, player, !hasPermission,
                permissionManager.getDenyMessage(), cost);
        Bukkit.getPluginManager().callEvent(stargateDestroyEvent);

        // Inform the player why the destruction was denied
        if (stargateDestroyEvent.getDeny()) {
            if (stargateDestroyEvent.getDenyReason() == null) {
                player.sendMessage(
                        Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.ADDON_INTERFERE));
            } else if (!stargateDestroyEvent.getDenyReason().isEmpty()) {
                player.sendMessage(stargateDestroyEvent.getDenyReason());
            }
            return true;
        }

        /*
         * If setting charge free destination is false, destination portal is
         * PortalFlag.Free and portal is of Fixed type or if player has override cost
         * permission, do not collect money
         */
        if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_DESTROY)
                && !Stargate.economyManager.chargeAndTax(player, stargateDestroyEvent.getCost())) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.LACKING_FUNDS));
            return true;
        }
        destroyAction.run();
        return false;
    }

}
