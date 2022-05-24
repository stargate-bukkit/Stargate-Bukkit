package net.TheDgtl.Stargate.util.portal;

import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.action.SupplierAction;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.manager.PermissionManager;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.EconomyHelper;

public class PortalDestructionHelper {

    /**
     * Destroys a portal if the entity has permission and can pay any fees
     *
     * @param event
     *                      <p>
     *                      The block break event triggering the destruction
     *                      </p>
     * @param portal
     *                      <p>
     *                      The portal to destroy
     *                      </p>
     * @param destroyAction
     *                      <p>
     *                      The action to run when destroying a portal
     *                      </p>
     * @return true if event should be canceled
     */
    public static boolean destroyPortalIfHasPermissionAndCanPay(Player player, Portal portal,
            Supplier<Boolean> destroyAction) {
        int cost = ConfigurationHelper.getInteger(ConfigurationOption.DESTROY_COST);
        StargateDestroyEvent stargateDestroyEvent = new StargateDestroyEvent(portal, player, cost);
        Bukkit.getPluginManager().callEvent(stargateDestroyEvent);
        PermissionManager permissionManager = new PermissionManager(player);
        if (permissionManager.hasDestroyPermissions((RealPortal) portal) && !stargateDestroyEvent.isCancelled()) {
            /*
             * If setting charge free destination is false, destination portal is
             * PortalFlag.Free and portal is of Fixed type or if player has override cost
             * permission, do not collect money
             */
            if (EconomyHelper.shouldChargePlayer(player, portal, BypassPermission.COST_DESTROY)
                    && !Stargate.economyManager.chargeAndTax(player, stargateDestroyEvent.getCost())) {
                player
                        .sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.LACKING_FUNDS));
                return true;
            }
            Stargate.syncTickPopulator.addAction(new SupplierAction(destroyAction));
        } else {
            return true;
        }
        return false;
    }
}
