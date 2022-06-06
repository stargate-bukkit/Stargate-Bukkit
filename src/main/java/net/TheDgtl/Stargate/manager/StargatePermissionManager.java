package net.TheDgtl.Stargate.manager;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import net.TheDgtl.Stargate.property.BypassPermission;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.TheDgtl.Stargate.util.portal.PortalPermissionHelper;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
public class StargatePermissionManager implements PermissionManager {

    private final Entity target;
    private String denyMessage;
    private Chat metadataProvider;
    private final boolean canProcessMetaData;
    private final LanguageManager languageManager;

    private static final String CREATE_PERMISSION = "sg.create";
    private static final String NETWORK_CREATE_PERMISSION = CREATE_PERMISSION + ".network";
    private static final String FLAG_PERMISSION = CREATE_PERMISSION + ".type.";


    private static final String DESTROY_OWNED = "sg.destroy.owned";
    private static final String USE_OWNED = "sg.use.owned";
    /**
     * Instantiates a new permission manager
     *
     * @param target <p>The entity to check permissions for</p>
     */
    public StargatePermissionManager(Entity target) {
        this(target, Stargate.getLanguageManagerStatic());
    }

    public StargatePermissionManager(Entity target, LanguageManager languageManager) {
        this.target = target;
        canProcessMetaData = setupMetadataProvider();
        this.languageManager = languageManager;
        Stargate.log(Level.CONFIG, "Checking permissions for entity " + target);
    }

    @Override
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
     * Checks whether the given entity has the given permission nodes
     *
     * @param entity      <p>The entity to check</p>
     * @param permissions <p>The permissions required</p>
     * @return <p>True if the entity has the given permissions</p>
     */
    private boolean hasPermissions(Entity entity, List<String> permissions) {
        for (String permission : permissions) {
            String message = " Checking permission '%s'. returned %s";
            boolean hasPermission = hasPermission(entity, permission);
            Stargate.log(Level.CONFIG, String.format(message, permission, hasPermission));
            if (!hasPermission) {
                denyMessage = determineTranslatableMessageFromPermission(permission);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given entity has the given permission node
     *
     * @param entity     <p>The entity to check</p>
     * @param permission <p>The permission required</p>
     * @return <p>True if the entity has the given permission</p>
     */
    private boolean hasPermission(Entity entity, String permission) {
        //Assume a null permission means no permission needed
        if (permission == null) {
            return true;
        }
        boolean hasGivenPermission = entity.hasPermission(permission);

        String parentPermission = getParentPermission(permission);
        if (parentPermission == null) {
            return hasGivenPermission;
        }

        //If the entity has the parent permission, allow unless explicitly defined
        if (hasPermission(entity, parentPermission)) {
            if (!entity.isPermissionSet(permission)) {
                return true;
            }
        }
        return hasGivenPermission;
    }

    /**
     * Gets the parent node of a given permission node
     *
     * @param permissionNode <p>The permission node to get the parent of</p>
     * @return <p>The permission's parent node</p>
     */
    private String getParentPermission(String permissionNode) {
        //Return null if no parent exists
        if (!permissionNode.contains(".")) {
            return null;
        }
        return permissionNode.substring(0, permissionNode.lastIndexOf("."));
    }

    @Override
    public boolean hasAccessPermission(RealPortal portal) {
        Stargate.log(Level.CONFIG, "Checking access permissions");
        if (portal.getOwnerUUID().equals(target.getUniqueId()) && hasPermission(target, USE_OWNED)) {
            Stargate.log(Level.CONFIG, "The player owns the portal, therefore has permission");
            return true;
        }
        List<String> relatedPerms = PortalPermissionHelper.getAccessPermissions(portal, target);
        return hasPermissions(target, relatedPerms);
    }

    @Override
    public boolean hasCreatePermissions(RealPortal portal) {
        Stargate.log(Level.CONFIG, "Checking create permissions");
        List<String> relatedPerms = PortalPermissionHelper.getCreatePermissions(portal, target);
        boolean hasPermission = hasPermissions(target, relatedPerms);
        if (hasPermission && portal.hasFlag(PortalFlag.PERSONAL_NETWORK) && canProcessMetaData && target instanceof Player) {
            return !isNetworkFull(portal.getNetwork());
        }
        return hasPermission;
    }

    @Override
    public boolean hasDestroyPermissions(RealPortal portal) {
        Stargate.log(Level.CONFIG, "Checking destroy permissions");
        if (portal.getOwnerUUID().equals(target.getUniqueId()) && hasPermission(target, DESTROY_OWNED)) {
            Stargate.log(Level.CONFIG, "The player owns the portal, therefore has permission");
            return true;
        }
        List<String> relatedPerms = PortalPermissionHelper.getDestroyPermissions(portal, target);
        return hasPermissions(target, relatedPerms);
    }

    @Override
    public boolean hasOpenPermissions(RealPortal entrance, Portal exit) {
        Stargate.log(Level.CONFIG, "Checking open permissions");
        if (entrance.getOwnerUUID().equals(target.getUniqueId()) && hasPermission(target, USE_OWNED)) {
            Stargate.log(Level.CONFIG, "The player owns the portal, therefore has permission");
            return true;
        }
        List<String> relatedPerms = PortalPermissionHelper.getOpenPermissions(entrance, exit, target);
        return hasPermissions(target, relatedPerms);
    }

    @Override
    public boolean hasTeleportPermissions(RealPortal entrance) {
        Stargate.log(Level.CONFIG, "Checking teleport permissions");
        List<String> relatedPerms = PortalPermissionHelper.getTeleportPermissions(entrance, target);
        boolean hasPermission = hasPermissions(target, relatedPerms);
        if (hasPermission && !entrance.isOpenFor(target) && target instanceof Player) {
            return canFollow();
        }
        return hasPermission;
    }

    /**
     * Determines the message to send the player based out of the permission it was denied.
     *
     * @param permissionNode <p>The permission node the entity was denied</p>
     * @return <p>The error message to display</p>
     */
    private String determineTranslatableMessageFromPermission(String permissionNode) {
        if (permissionNode.equals("sg.use.follow")) {
            return languageManager.getErrorMessage(TranslatableMessage.TELEPORTATION_OCCUPIED);
        }

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
                Stargate.log(Level.CONFIG, String.format(" Network is full, maxGates = %s", maxGates));
                return true;
            }
        }
        Stargate.log(Level.CONFIG, " Network is not full, maxGates = %s");
        return false;
    }

    /**
     * Check the meta can-followthrough and determine if entity has permission
     *
     * @return <p> If the entity has the meta </p>
     */
    private boolean canFollow() {
        String metaString = "can-followthrough";
        Player player = (Player) target;
        String group = metadataProvider.getPrimaryGroup(metaString, player);
        boolean canFollowThrough = (metadataProvider.getPlayerInfoBoolean(target.getWorld().getName(), player,
                metaString, true)
                && metadataProvider.getGroupInfoBoolean(target.getWorld().getName(), group, metaString, true));

        Stargate.log(Level.CONFIG, String.format(" Checking 'can-followthrough' meta. Returned %s", canFollowThrough));
        if (!canFollowThrough) {
            denyMessage = languageManager.getErrorMessage(TranslatableMessage.TELEPORTATION_OCCUPIED);
        }
        return canFollowThrough;
    }

    @Override
    public boolean canCreateInNetwork(String network) {
        if (network == null) {
            return false;
        }

        HighlightingStyle highlight = HighlightingStyle.getHighlightType(network);
        String networkName = HighlightingStyle.getNameFromHighlightedText(network);
        boolean hasPermission;

        switch (highlight) {
            case PERSONAL:
                if (target.getName().equals(networkName)) {
                    hasPermission = target.hasPermission(NETWORK_CREATE_PERMISSION + ".personal");
                } else {
                    hasPermission = target.hasPermission(BypassPermission.PRIVATE.getPermissionString());
                }
                break;
            case BUNGEE:
                hasPermission = target.hasPermission(NETWORK_CREATE_PERMISSION + ".type." + PortalFlag.FANCY_INTER_SERVER);
                break;
            default:
                if (networkName.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
                    hasPermission = target.hasPermission(NETWORK_CREATE_PERMISSION + ".default");
                } else if (networkName.equals(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK))) {
                    //It's not possible to create a non-bungee portal on this network
                    hasPermission = false;
                } else {
                    hasPermission = target.hasPermission(PortalPermissionHelper.generateCustomNetworkPermission(CREATE_PERMISSION, networkName));
                }
                break;
        }
        if (!hasPermission) {
            denyMessage = languageManager.getErrorMessage(TranslatableMessage.NET_DENY);
        }
        return hasPermission;
    }

    @Override
    public String getDenyMessage() {
        return denyMessage;
    }

}
