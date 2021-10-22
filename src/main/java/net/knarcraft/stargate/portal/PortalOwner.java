package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The portal owner represents the owner of a portal
 */
public class PortalOwner {

    private UUID ownerUUID;
    private String ownerName;

    /**
     * Instantiates a new portal owner
     *
     * @param ownerIdentifier <p>A UUID, or a username for legacy support</p>
     */
    public PortalOwner(String ownerIdentifier) {
        parseIdentifier(ownerIdentifier);
    }

    /**
     * Instantiates a new portal owner
     *
     * @param player <p>The player which is the owner of the portal</p>
     */
    public PortalOwner(Player player) {
        this.ownerUUID = player.getUniqueId();
        this.ownerName = player.getName();
    }

    /**
     * Gets the UUID of this owner
     *
     * @return <p>The UUID of this owner, or null if a UUID is not available</p>
     */
    public UUID getUUID() {
        return ownerUUID;
    }

    /**
     * Gets the name of this owner
     *
     * @return <p>The name of this owner</p>
     */
    public String getName() {
        return ownerName;
    }

    /**
     * Gets the one identifier used for saving the owner
     *
     * <p>If the UUID is available, a string representation of the UUID will be returned. If not, the owner's name will
     * be returned.</p>
     *
     * @return <p>The owner's identifier</p>
     */
    public String getIdentifier() {
        if (ownerUUID != null) {
            return ownerUUID.toString();
        } else {
            return ownerName;
        }
    }

    /**
     * Parses the identifier of a portal's owner
     *
     * <p>The identifier should be a valid UUID, but can be a username of max 16 characters for legacy support. Strings
     * longer than 16 characters not parse-able as a UUID will silently fail by setting the owner name to the
     * identifier.</p>
     *
     * @param ownerIdentifier <p>The identifier for a portal's owner</p>
     */
    private void parseIdentifier(String ownerIdentifier) {
        UUID ownerUUID = null;
        String ownerName;
        if (ownerIdentifier.length() > 16) {
            //If more than 16 characters, the string cannot be a username, so it's probably a UUID
            try {
                ownerUUID = UUID.fromString(ownerIdentifier);
                OfflinePlayer offlineOwner = Bukkit.getServer().getOfflinePlayer(ownerUUID);
                ownerName = offlineOwner.getName();
            } catch (IllegalArgumentException ex) {
                //Invalid as UUID and username, so just keep it as owner name and hope the server owner fixes it
                ownerName = ownerIdentifier;
                Stargate.debug("loadAllPortals", "Invalid stargate owner string: " + ownerIdentifier);
            }
        } else {
            //Old username from the pre-UUID times. Just keep it as the owner name
            ownerName = ownerIdentifier;
        }
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
    }

}
