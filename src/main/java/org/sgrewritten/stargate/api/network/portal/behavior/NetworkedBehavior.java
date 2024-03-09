package org.sgrewritten.stargate.api.network.portal.behavior;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateAccessPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateActivatePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.property.MetadataType;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.util.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class NetworkedBehavior extends AbstractPortalBehavior {
    private static final int NO_DESTINATION_SELECTED = -1;
    private List<Portal> destinations;
    private int selectedDestination = NO_DESTINATION_SELECTED;
    private long previousDestinationSelectionTime;
    private String loadedDestination;

    public NetworkedBehavior(LanguageManager languageManager) {
        super(languageManager);
    }

    @Override
    public void update() {
        Portal destination;
        if (portal.hasFlag(StargateFlag.ALWAYS_ON) && this.loadedDestination != null) {
            destination = portal.getNetwork().getPortal(this.loadedDestination);
            this.loadedDestination = null;
        } else {
            destination = getDestination();
        }
        if (portal.isActive() && (destination == null || portal.getNetwork().getPortal(destination.getName()) == null)) {
            portal.deactivate();
            portal.close(true);
        }

        setSelectedDestination(reloadSelectedDestination(destination));
    }

    /**
     * Calculate the position of the portal that is selected, assuming the available destinations have changed.
     *
     * @param destination <p>The previously selected portal</p>
     * @return <p> The position of the selected portal in the destinations list</p>
     */
    private int reloadSelectedDestination(Portal destination) {
        Player player;
        if (portal.getActivatorUUID() == null || portal.hasFlag(StargateFlag.ALWAYS_ON)) {
            player = null;
        } else {
            player = Bukkit.getPlayer(portal.getActivatorUUID());
        }
        destinations = getDestinations(player);
        if (destinations.contains(destination)) {
            return destinations.indexOf(destination);
        }
        return NO_DESTINATION_SELECTED;
    }


    /**
     * Gets the destinations available to the given player
     *
     * @param player <p>
     *               The player to get destinations for
     *               </p>
     * @return <p>
     * The destinations available to the player
     * </p>
     */
    private List<Portal> getDestinations(@Nullable Player player) {
        List<String> availablePortals;
        Network network = portal.getNetwork();
        if (player == null) {
            availablePortals = new ArrayList<>(network.getAllPortals().stream().map(Portal::getId).toList());
            availablePortals.remove(portal.getId());
        } else {
            availablePortals = new ArrayList<>(network.getAvailablePortals(player, portal));
        }
        Collections.sort(availablePortals);
        List<Portal> output = new ArrayList<>();
        for (String name : availablePortals) {
            output.add(network.getPortal(name));
        }
        return output;
    }


    @Override
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        super.onSignClick(event);
        if (event.getPlayer().isSneaking()) {
            return;
        }
        Player actor = event.getPlayer();
        if ((portal.getActivatorUUID() != null && !actor.getUniqueId().equals(portal.getActivatorUUID()))
                || (portal.isOpen() && !portal.hasFlag(StargateFlag.ALWAYS_ON))) {
            return;
        }

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), super.languageManager);
        if (!hasActivatePermissions(actor, permissionManager)) {
            Stargate.log(Level.CONFIG, "Player did not have permission to activate portal");
            return;
        }

        boolean previouslyActivated = portal.isActive();
        portal.activate(actor);
        if (destinations.isEmpty()) {
            String message = super.languageManager.getErrorMessage(TranslatableMessage.DESTINATION_EMPTY);
            MessageUtils.sendMessageFromPortal(portal, event.getPlayer(), message, MessageType.DESTINATION_EMPTY);

            portal.deactivate();
            return;
        }

        setSelectedDestination(selectNewDestination(event.getAction(), previouslyActivated));
        portal.updateState();
    }

    /**
     * Selects the next destination in this portal's destination list
     *
     * @param action              <p>The action performed on this portal's sign</p>
     * @param previouslyActivated <p>Whether this portal is currently active</p>
     * @return <p>The index of the new destination</p>
     */
    private int selectNewDestination(Action action, boolean previouslyActivated) {
        if (!previouslyActivated) {
            if (!ConfigurationHelper.getBoolean(ConfigurationOption.REMEMBER_LAST_DESTINATION)) {
                return 0;
            } else {
                if (getSelectedDestination() == NO_DESTINATION_SELECTED) {
                    setSelectedDestination(0);
                }
                return getSelectedDestination();
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
            int step = (action == Action.RIGHT_CLICK_BLOCK) ? 1 : -1;
            return getNextDestination(step, getSelectedDestination());
        }
        return getSelectedDestination();
    }

    /**
     * Gets the next destination index
     *
     * <p>A method which allows selecting an index x steps away from a reference index
     * without having to bother with index out of bounds stuff. If the index is out
     * of bounds, it will just start counting from 0></p>
     *
     * @param step               <p>The amount of steps to move down the list of destinations</p>
     * @param initialDestination <p>The currently selected destination</p>
     * @return <p>The next destination index</p>
     */
    private int getNextDestination(int step, int initialDestination) {
        int destinationLength = destinations.size();
        // Avoid infinite recursion if this is the only gate available
        if (destinationLength < 1) {
            return -1;
        }
        int destination = initialDestination + destinationLength;
        return (destination + step) % destinationLength;
    }

    /**
     * Checks whether the given player is allowed to activate this portal
     *
     * @param player <p>The player to check permissions of</p>
     * @return <p>True if the given player is allowed to activate this portal</p>
     */
    private boolean hasActivatePermissions(Player player, StargatePermissionManager permissionManager) {
        boolean hasPermission = permissionManager.hasAccessPermission(portal);
        StargateAccessPortalEvent accessEvent = new StargateAccessPortalEvent(player, portal, !hasPermission,
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            String message = null;
            if (accessEvent.getDenyReason() == null) {
                message = super.languageManager.getErrorMessage(TranslatableMessage.ADDON_INTERFERE);
            } else if (!accessEvent.getDenyReason().isEmpty()) {
                message = accessEvent.getDenyReason();
            }
            MessageUtils.sendMessageFromPortal(portal, player, message, MessageType.DENY);
            if (hasPermission) {
                Stargate.log(Level.CONFIG, " Access event was denied externally");
            }
            return false;
        }

        //Call the activate event to notify add-ons
        StargateActivatePortalEvent event = new StargateActivatePortalEvent(portal, player, getPortalNames(destinations),
                this.getDestinationName());
        Bukkit.getPluginManager().callEvent(event);

        this.destinations = getPortals(event.getDestinations());
        // TODO: Modify the destination based on event outcome
        return true;
    }

    /**
     * Gets a list of portals from a list of portal names
     *
     * @param names <p>The list of portal names to get portals from</p>
     * @return <p>The portals corresponding to the names</p>
     */
    private List<Portal> getPortals(List<String> names) {
        List<Portal> portals = new ArrayList<>(names.size());
        names.forEach(item -> portals.add(portal.getNetwork().getPortal(item)));
        return portals;
    }

    /**
     * Gets a list of portal names from a list of portals
     *
     * @param portals <p>The list of portals to get the names of</p>
     * @return <p>The names of the portals</p>
     */
    private List<String> getPortalNames(List<Portal> portals) {
        List<String> names = new ArrayList<>(portals.size());
        portals.forEach(item -> names.add(item.getName()));
        return names;
    }

    private void setSelectedDestination(int selectedDestination) {
        if (portal.hasFlag(StargateFlag.ALWAYS_ON)) {
            final long currentTime = System.currentTimeMillis();
            this.previousDestinationSelectionTime = currentTime;
            /**
             * Avoid unnecessary database spam whenever the destination has changed.
             */
            new StargateGlobalTask() {
                @Override
                public void run() {
                    Portal destination = getDestination();
                    if (currentTime == previousDestinationSelectionTime && destination != null) {
                        /*
                         * setSelectedDestination(int) can be called multiple times within the same millisecond, this avoids
                         * duplicate unnecessary calls
                         */
                        previousDestinationSelectionTime = -1;
                        new StargateQueuedAsyncTask() {
                            @Override
                            public void run() {
                                portal.setMetadata(new JsonPrimitive(destination.getId()), MetadataType.DESTINATION.name());
                            }
                        }.runNow();
                    }
                }
            }.runDelayed(20);
        }
        this.selectedDestination = selectedDestination;
    }

    private int getSelectedDestination() {
        return this.selectedDestination;
    }

    @Override
    public Portal getDestination() {
        if (getSelectedDestination() == NO_DESTINATION_SELECTED || getSelectedDestination() >= destinations.size()) {
            return null;
        }
        return destinations.get(getSelectedDestination());
    }

    @Override
    public @NotNull LineData @NotNull [] getLines() {
        LineData[] lines = new LineData[4];
        lines[0] = new PortalLineData(portal, SignLineType.THIS_PORTAL);
        if (!portal.isActive() || this.getSelectedDestination() == NO_DESTINATION_SELECTED) {
            lines[1] = new TextLineData(super.languageManager.getString(TranslatableMessage.RIGHT_CLICK), SignLineType.TEXT);
            lines[2] = new TextLineData(super.languageManager.getString(TranslatableMessage.TO_USE), SignLineType.TEXT);
            lines[3] = new NetworkLineData(portal.getNetwork());
        } else {
            drawActiveSign(lines);
        }
        return lines;
    }

    /**
     * Draws an active networked portal sign
     *
     * @param lines <p>The sign lines to update</p>
     */
    private void drawActiveSign(LineData[] lines) {
        int destinationIndex = getSelectedDestination() % 3;
        int firstDestination = getSelectedDestination() - destinationIndex;
        int maxLength = destinations.size();
        for (int lineIndex = 0; lineIndex < 3; lineIndex++) {
            int destination = lineIndex + firstDestination;
            if (destination >= maxLength) {
                lines[lineIndex + 1] = new TextLineData();
                continue;
            }
            drawDestination(lineIndex, destination, destinationIndex, lines);
        }
    }

    /**
     * Draws one destination on this portal's sign
     *
     * @param lineIndex        <p>The index of the line to draw</p>
     * @param destination      <p>The line to draw the selectors at</p>
     * @param destinationIndex <p>The modulo of the selected destination</p>
     * @param lines            <p>The sign lines to update</p>
     */
    private void drawDestination(int lineIndex, int destination, int destinationIndex, LineData[] lines) {
        boolean isSelectedPortal = (destinationIndex == lineIndex);
        Portal destinationPortal = destinations.get(destination);
        lines[lineIndex + 1] = new PortalLineData(destinationPortal, isSelectedPortal ? SignLineType.DESTINATION_PORTAL : SignLineType.PORTAL);
    }

    @Override
    public @NotNull StargateFlag getAttachedFlag() {
        return StargateFlag.NETWORKED;
    }

    @Override
    public void assignPortal(@NotNull RealPortal portal) {
        super.assignPortal(portal);
        if (!portal.hasFlag(StargateFlag.ALWAYS_ON)) {
            return;
        }
        JsonElement destinationElement = portal.getMetadata(MetadataType.DESTINATION.name());
        if (destinationElement == null) {
            return;
        }
        this.loadedDestination = destinationElement.getAsString();
        portal.activate(null);
    }
}
