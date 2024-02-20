package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.portal.property.PortalOwner;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Helps migrate player names to UUID where necessary
 */
public final class UUIDMigrationHelper {

    private UUIDMigrationHelper() {

    }

    private static Map<String, List<Portal>> playerNamesToMigrate;

    /**
     * Migrates the player's name to a UUID
     *
     * <p>If any portals are missing a UUID for their owner, and the given player is the owner of those portals, the
     * given player's UUID will be used as UUID for the portals' owner.</p>
     *
     * @param player <p>The player to migrate</p>
     */
    public static void migrateUUID(@NotNull OfflinePlayer player) {
        Map<String, List<Portal>> playersToMigrate = getPlayersToMigrate();
        String playerName = player.getName();

        //Nothing to do
        if (!playersToMigrate.containsKey(playerName)) {
            return;
        }

        Stargate.debug("UUIDMigrationHelper::migrateUUID", String.format("Migrating name to UUID for player %s",
                playerName));
        List<Portal> portalsOwned = playersToMigrate.get(playerName);
        if (portalsOwned == null) {
            return;
        }

        migratePortalsToUUID(portalsOwned, player.getUniqueId());

        //Remove the player to prevent the migration from happening every time the player joins
        playersToMigrate.remove(playerName);
    }

    /**
     * Migrates a list of portals to use UUID instead of only player name
     *
     * @param portals  <p>The portals to migrate</p>
     * @param uniqueId <p>The unique ID of the portals' owner</p>
     */
    private static void migratePortalsToUUID(@NotNull List<Portal> portals, @NotNull UUID uniqueId) {
        Set<World> worldsToSave = new HashSet<>();

        //Get the real portal from the copy and set UUID
        for (Portal portalCopy : portals) {
            Portal portal = PortalHandler.getByName(portalCopy.getCleanName(), portalCopy.getCleanNetwork());
            if (portal != null) {
                portal.getOwner().setUUID(uniqueId);
                worldsToSave.add(portal.getWorld());
            }
        }

        //Need to make sure the changes are saved
        for (World world : worldsToSave) {
            PortalFileHelper.saveAllPortals(world);
        }
    }

    /**
     * Gets all player names which need to be migrated to UUIDs
     *
     * @return <p>The player names to migrate</p>
     */
    @NotNull
    private static Map<String, List<Portal>> getPlayersToMigrate() {
        //Make sure to only go through portals once
        if (playerNamesToMigrate != null) {
            return playerNamesToMigrate;
        }

        playerNamesToMigrate = new HashMap<>();
        for (Portal portal : PortalRegistry.getAllPortals()) {
            PortalOwner owner = portal.getOwner();
            String ownerName = owner.getName();

            //If a UUID is missing, add the portal to the list owned by the player
            if (owner.getUUID() == null) {
                List<Portal> portalList = playerNamesToMigrate.get(ownerName);
                if (portalList == null) {
                    List<Portal> newList = new ArrayList<>();
                    newList.add(portal);
                    playerNamesToMigrate.put(ownerName, newList);
                } else {
                    portalList.add(portal);
                }
            }
        }
        return playerNamesToMigrate;
    }

}
