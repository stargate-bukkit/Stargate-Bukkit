package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateEvent;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages the plugin's permissions.
 *
 * @author Thorin
 * @author Pheotis
 */

// TODO See Discussion Three
public class PermissionManager {
    private Entity player;
    private TranslatableMessage denyMsg;
    private Chat metadataProvider;
    private boolean canProcessMetaData;

    private final static String FLAGPERMISSION = "sg.create.type.";
    private final static String CREATEPERMISSION = "sg.create.network";

    public PermissionManager(Entity target) {
        this.player = target;
        canProcessMetaData = setupMetadataProvider();
    }

    public EnumSet<PortalFlag> returnAllowedFlags(EnumSet<PortalFlag> flags) {
        for (PortalFlag flag : flags) {
            if (flag == PortalFlag.PERSONAL_NETWORK || flag == PortalFlag.IRON_DOOR)
                continue;

            if (!player.hasPermission((FLAGPERMISSION + flag.label).toLowerCase())) {
                flags.remove(flag);
            }
        }
        return flags;
    }

    /**
     * @return true if succesfull
     */
    private boolean setupMetadataProvider() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        metadataProvider = rsp.getProvider();
        return metadataProvider != null;
    }

    public boolean hasPerm(String permission) {
        return player.hasPermission(permission);
    }

    public boolean hasPerm(StargateEvent event) {
        List<Permission> perms = event.getRelatedPerms();
        
        if(player instanceof Player)
            Stargate.log(Level.FINEST, "checking perm for player " + ((Player)player).getName());
        
        for (Permission perm : perms) {
            Stargate.log(Level.FINEST, " checking permission " + ((perm != null) ? perm.getName() : "null"));
            if (!player.hasPermission(perm)) {
                //TODO messaging
                // denyMsg = LangMsg.<something>
                denyMsg = TranslatableMessage.NET_DENY;
                return false;
            }
        }

        if ((event instanceof StargateCreateEvent) && event.getPortal().hasFlag(PortalFlag.PERSONAL_NETWORK)
                && canProcessMetaData) {

            int maxGates = metadataProvider.getPlayerInfoInteger(player.getWorld().getName(), (Player) player, "gate-limit", -1);
            if (maxGates == -1) {
                metadataProvider.getGroupInfoInteger(player.getWorld(), metadataProvider.getPrimaryGroup((Player) player), "gate-limit", -1);
            }
            if (maxGates == -1) {
                maxGates = Setting.getInteger(Setting.GATE_LIMIT);
            }

            if (maxGates > -1 && !player.hasPermission(Bypass.GATE_LIMIT.getPermissionString())) {
                Network net = event.getPortal().getNetwork();
                int currentAmount = net.size();

                if (currentAmount >= maxGates) {
                    //TODO messaging , gatelimit reached
                    denyMsg = TranslatableMessage.NET_FULL;
                    // denyMsg = LangMsg.<something>
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canCreateInNetwork(String network) {
        if (network == null)
            return false;

        boolean hasPerm;

        if (player.getName().equals(network))
            hasPerm = player.hasPermission(CREATEPERMISSION + ".personal");
        else if (network.equals(Setting.getString(Setting.DEFAULT_NET)))
            hasPerm = player.hasPermission(CREATEPERMISSION + ".default");
        else
            hasPerm = player.hasPermission(CREATEPERMISSION + ".custom." + network);
        if (!hasPerm)
            denyMsg = TranslatableMessage.NET_DENY;
        return hasPerm;
    }

    public TranslatableMessage getDenyMsg() {
        return denyMsg;
    }
}
