package net.TheDgtl.Stargate.manager;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateEvent;
import net.TheDgtl.Stargate.event.StargatePortalEvent;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.PortalPermissionHelper;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages the plugin's permissions.
 *
 * @author Thorin
 * @author Pheotis
 */

public class PermissionManager {

    private final Entity target;
    private String denyMessage;
    private Chat metadataProvider;
    private final boolean canProcessMetaData;
    private final LanguageManager languageManager;

    private static final String FLAG_PERMISSION = "sg.create.type.";
    private static final String CREATE_PERMISSION = "sg.create.network";

    /**
     * Instantiates a new permission manager
     *
     * @param target <p>The entity to check permissions for</p>
     */
    public PermissionManager(Entity target) {
        this(target, Stargate.languageManager);
    }

    public PermissionManager(Entity target, LanguageManager languageManager) {
        this.target = target;
        canProcessMetaData = setupMetadataProvider();
        this.languageManager = languageManager;
    }

    /**
     * Gets all flags usable by the entity
     *
     * @param flags <p>The flags to check if the entity can use</p>
     * @return <p>The flags usable by the entity</p>
     */
    public Set<PortalFlag> returnDisallowedFlags(Set<PortalFlag> flags) {
        Set<PortalFlag> disallowed = EnumSet.noneOf(PortalFlag.class);
        for (PortalFlag flag : flags) {
            if (flag == PortalFlag.PERSONAL_NETWORK || flag == PortalFlag.IRON_DOOR || flag == PortalFlag.FIXED || flag == PortalFlag.NETWORKED) {
                continue;
            }

            if (!target.hasPermission((FLAG_PERMISSION + flag.getCharacterRepresentation()).toLowerCase())) {
                disallowed.add(flag);
            }
        }
        return disallowed;
    }

    /**
     * Tries to set up the vault chat metadata provider
     *
     * @return <p>True if successful</p>
     */
    private boolean setupMetadataProvider() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Chat> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (registeredServiceProvider == null) {
            return false;
        }
        metadataProvider = registeredServiceProvider.getProvider();
        return true;
    }
    
    /**
     * Scrolls through a list of permissions and notes if 
     * @param permissions
     * @return
     */
    private boolean hasPermission(List<Permission> permissions) {
        for (Permission relatedPermission : permissions) {
            String message = " Checking permission '%s'. returned %s";
            boolean hasPermission = relatedPermission == null || target.hasPermission(relatedPermission);
            String permissionNode =  (relatedPermission != null) ? relatedPermission.getName() : "null";
            Stargate.log(Level.CONFIG, String.format(message,permissionNode,hasPermission));
            if (!hasPermission) {
                denyMessage = determineTranslatableMessageFromPermission(relatedPermission);
                return false;
            }
        }
        return true;
    }
    
    public boolean hasAccessPermission(RealPortal portal) {
        List<Permission> relatedPerms = PortalPermissionHelper.getAccessPermissions(portal, target);
        return hasPermission(relatedPerms);
    }
    
    public boolean hasCreatePermissions(RealPortal portal) {
        List<Permission> relatedPerms = PortalPermissionHelper.getCreatePermissions(portal, target);
        boolean hasPermission = hasPermission(relatedPerms);
        if (hasPermission && portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && canProcessMetaData && target instanceof Player) {
            return !isNetworkFull(portal.getNetwork());
        }
        return hasPermission;
    }
    
    public boolean hasDestroyPermissions(RealPortal portal) {
        List<Permission> relatedPerms = PortalPermissionHelper.getDestroyPermissions(portal, target);
        return hasPermission(relatedPerms);
    }
    
    public boolean hasOpenPermissions(RealPortal entrance, Portal exit) {
        List<Permission> relatedPerms = PortalPermissionHelper.getOpenPermissions(entrance, exit, target);
        return hasPermission(relatedPerms);
    }
    
    public boolean hasTeleportPermissions(RealPortal entrance) {
        List<Permission> relatedPerms = PortalPermissionHelper.getTeleportPermissions(entrance, target);
        boolean hasPermission = hasPermission(relatedPerms);
        if (hasPermission && ! entrance.isOpenFor(target) && target instanceof Player) {
            return canFollow();
        }
        return hasPermission;
    }

    public String determineTranslatableMessageFromPermission(Permission permission) {
        String permissionNode = permission.getName();
        if (permissionNode.contains("create") || permissionNode.contains("use")) {
            if (permissionNode.contains("world")) {
                String unformattedMessage = languageManager.getErrorMessage(TranslatableMessage.WORLD_DENY);
                String worldName = permissionNode.split(".world.")[1];
                return TranslatableMessageFormatter.formatWorld(unformattedMessage, worldName);
            }
            if (permissionNode.contains("network")) {
                return languageManager.getErrorMessage(TranslatableMessage.NET_DENY);
            }
        }

        if (permissionNode.contains("create")) {
            if (permissionNode.contains("design")) {
                return languageManager.getErrorMessage(TranslatableMessage.GATE_DENY);
            }
            if (permissionNode.contains("type")) {
                PortalFlag flag = PortalFlag.valueOf(permissionNode.split(".type.")[1]);
                if (flag == PortalFlag.BUNGEE || flag == PortalFlag.FANCY_INTER_SERVER) {
                    return languageManager.getErrorMessage(TranslatableMessage.BUNGEE_DENY);
                }
            }
        }
        return languageManager.getErrorMessage(TranslatableMessage.DENY);

    }

    /**
     * Checks if the network given in a stargate create event is full
     *
     * @param network <p>The network to check</p>
     * @return <p>True if the network is full</p>
     */
    private boolean isNetworkFull(Network network) {
        Player player = (Player) target;
        String metaString = "gate-limit";

        int maxGates = metadataProvider.getPlayerInfoInteger(target.getWorld().getName(), player, metaString, -1);
        if (maxGates == -1) {
            metadataProvider.getGroupInfoInteger(target.getWorld(),
                    metadataProvider.getPrimaryGroup(player), metaString, -1);
        }
        if (maxGates == -1) {
            maxGates = ConfigurationHelper.getInteger(ConfigurationOption.GATE_LIMIT);
        }

        if (maxGates > -1 && !target.hasPermission(BypassPermission.GATE_LIMIT.getPermissionString())) {
            int existingGatesInNetwork = network.size();

            if (existingGatesInNetwork >= maxGates) {
                denyMessage = languageManager.getErrorMessage(TranslatableMessage.NET_FULL);
                return true;
            }
        }
        return false;
    }

    private boolean canFollow() {
        String metaString = "can-followthrough";
        Player player = (Player) target;
        String group = metadataProvider.getPrimaryGroup(metaString, player);
        return metadataProvider.getPlayerInfoBoolean(target.getWorld().getName(), player, metaString, true) &&
                metadataProvider.getGroupInfoBoolean(target.getWorld().getName(), group, metaString, true);
    }

    /**
     * Checks whether the entity is allowed to create stargates in the given network
     *
     * @param network <p>The formated name of the network to check</p>
     * @return <p>True if the entity is allowed to create stargates</p>
     */
    public boolean canCreateInNetwork(String network) {
        if (network == null) {
            return false;
        }

        HighlightingStyle highlight = HighlightingStyle.getHighlightType(network);
        String netName = HighlightingStyle.getNameFromHighlightedText(network);
        boolean hasPermission;

        switch(highlight) {
        case PERSONAL:
            if (target.getName().equals(netName)) {
                hasPermission = target.hasPermission(CREATE_PERMISSION + ".personal");
            } else {
                hasPermission = target.hasPermission(BypassPermission.PRIVATE.getPermissionString());
            }
            break;
        case BUNGEE:
            hasPermission = target.hasPermission(CREATE_PERMISSION + ".type." + PortalFlag.FANCY_INTER_SERVER);
            break;
        default:
            if (netName.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
                hasPermission = target.hasPermission(CREATE_PERMISSION + ".default");
            } else {
                hasPermission = target.hasPermission(CREATE_PERMISSION + ".custom." + netName);
            }
            break;
        }
        if (!hasPermission) {
            denyMessage = languageManager.getErrorMessage(TranslatableMessage.NET_DENY);
        }
        return hasPermission;
    }

    /**
     * Gets the deny-message to display if a previous permission check returned false
     *
     * @return <p>The message to display when telling the player the action has been denied</p>
     */
    public String getDenyMessage() {
        return denyMessage;
    }

}
