package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.event.StargateAccessEvent;
import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.manager.StargatePermissionManager;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A portal that is part of a network
 *
 * <p>For a networked portal, all portals in its network are its destinations</p>
 */
public class NetworkedPortal extends AbstractPortal {

    private static final int NO_DESTINATION_SELECTED = -1;

    private int selectedDestination = NO_DESTINATION_SELECTED;
    private List<Portal> destinations = new ArrayList<>();

    private boolean isActive;

    /**
     * Instantiates a new networked portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    public NetworkedPortal(Network network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID,
                           StargateLogger logger) throws NameErrorException {
        super(network, name, flags, gate, ownerUUID, logger);
    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {
        super.onSignClick(event);
        if (event.getPlayer().isSneaking()) {
            return;
        }
        Player actor = event.getPlayer();
        if ((super.activator != null && !actor.getUniqueId().equals(super.activator))
                || (this.isOpen() && !hasFlag(PortalFlag.ALWAYS_ON))) {
            return;
        }

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer());
        if (!hasActivatePermissions(actor, permissionManager)) {
            Stargate.log(Level.CONFIG, "Player did not have permission to activate portal");
            return;
        }

        boolean previouslyActivated = this.isActive;
        activate(actor);
        if (destinations.size() < 1) {
            String message = Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.DESTINATION_EMPTY);
            event.getPlayer().sendMessage(message);
            this.isActive = false;
            return;
        }

        selectedDestination = selectNewDestination(event.getAction(), previouslyActivated);
        this.updateState();
        if (hasFlag(PortalFlag.ALWAYS_ON)) {
            super.destination = getDestination();
        }
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
                if (selectedDestination == NO_DESTINATION_SELECTED) {
                    selectedDestination = 0;
                }
                return selectedDestination;
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
            int step = (action == Action.RIGHT_CLICK_BLOCK) ? 1 : -1;
            return getNextDestination(step, selectedDestination);
        }
        return selectedDestination;
    }

    @Override
    public void onButtonClick(PlayerInteractEvent event) {
        if (!event.getPlayer().getUniqueId().equals(activator)) {
            return;
        }
        super.onButtonClick(event);
    }

    @Override
    public void updateState() {
        Portal destination = getDestination();
        if (this.isActive && (destination == null || network.getPortal(destination.getName()) == null)) {
            this.deactivate();
            this.isActive = false; // in case of alwaysOn portal
            this.close(true);
        }

        this.selectedDestination = reloadSelectedDestination();
        super.updateState();
    }

    /**
     * Calculate the position of the portal that is selected, assuming the available destinations have changed.
     *
     * @return <p> The position of the selected portal in the destinations list</p>
     */
    private int reloadSelectedDestination() {
        if (!this.isActive) {
            return NO_DESTINATION_SELECTED;
        }

        Portal destination = this.destinations.get(this.selectedDestination);
        destinations = getDestinations(Bukkit.getPlayer(activator));
        if (destinations.contains(destination)) {
            return destinations.indexOf(destination);
        }
        return NO_DESTINATION_SELECTED;
    }


    @Override
    public void close(boolean force) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !force || super.isDestroyed) {
            return;
        }
        super.close(force);
        deactivate();
    }

    @Override
    public void drawControlMechanisms() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        if (!this.isActive) {
            lines[1] = super.colorDrawer.formatLine(Stargate.getLanguageManagerStatic().getString(TranslatableMessage.RIGHT_CLICK));
            lines[2] = super.colorDrawer.formatLine(Stargate.getLanguageManagerStatic().getString(TranslatableMessage.TO_USE));
            lines[3] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatLine(network.getHighlightedName()) : "";
        } else {
            drawActiveSign(lines);
        }
        getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public Portal getDestination() {
        if (selectedDestination == NO_DESTINATION_SELECTED || selectedDestination >= destinations.size()) {
            return null;
        }
        return destinations.get(selectedDestination);
    }

    /**
     * Draws an active networked portal sign
     *
     * @param lines <p>The sign lines to update</p>
     */
    private void drawActiveSign(String[] lines) {
        int destinationIndex = selectedDestination % 3;
        int firstDestination = selectedDestination - destinationIndex;
        int maxLength = destinations.size();
        for (int lineIndex = 0; lineIndex < 3; lineIndex++) {
            int destination = lineIndex + firstDestination;
            if (destination == maxLength) {
                break;
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
    private void drawDestination(int lineIndex, int destination, int destinationIndex, String[] lines) {
        if (ConfigurationHelper.getInteger(ConfigurationOption.NAME_STYLE) == 1) {
            if (destinationIndex == lineIndex) {
                lines[lineIndex + 1] = super.colorDrawer.formatPortalName(destinations.get(destination),
                        HighlightingStyle.DESTINATION);
            } else {
                lines[lineIndex + 1] = super.colorDrawer.formatLine(destinations.get(destination).getName());
            }
            return;
        }

        HighlightingStyle highlightingStyle;
        if (destinationIndex == lineIndex) {
            highlightingStyle = HighlightingStyle.DESTINATION;
        } else {
            highlightingStyle = HighlightingStyle.NOTHING;
        }
        lines[lineIndex + 1] = super.colorDrawer.formatPortalName(destinations.get(destination), highlightingStyle);
    }

    /**
     * Gets the destinations available to the given player
     *
     * @param player <p>The player to get destinations for</p>
     * @return <p>The destinations available to the player</p>
     */
    private List<Portal> getDestinations(Player player) {
        if (player == null) {
            return new ArrayList<>();
        }
        List<String> availablePortals = new ArrayList<>(network.getAvailablePortals(player, this));
        Collections.sort(availablePortals);
        List<Portal> destinations = new ArrayList<>();
        for (String name : availablePortals) {
            destinations.add(network.getPortal(name));
        }
        return destinations;
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
        boolean hasPermission = permissionManager.hasAccessPermission(this);
        StargateAccessEvent accessEvent = new StargateAccessEvent(player, this, !hasPermission,
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            if (accessEvent.getDenyReason() == null) {
                player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.ADDON_INTERFERE));
            } else if (!accessEvent.getDenyReason().isEmpty()) {
                player.sendMessage(accessEvent.getDenyReason());
            }
            if (hasPermission) {
                Stargate.log(Level.CONFIG, " Access event was denied externally");
            }
            return false;
        }

        //Call the activate event to notify add-ons
        StargateActivateEvent event = new StargateActivateEvent(this, player, getPortalNames(destinations),
                this.getDestinationName());
        Bukkit.getPluginManager().callEvent(event);

        //Update this sign's displayed destinations
        destinations = getPortals(event.getDestinations());
        destination = network.getPortal(event.getDestination());
        drawControlMechanisms();
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
        names.forEach((item) -> portals.add(network.getPortal(item)));
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
        portals.forEach((item) -> names.add(item.getName()));
        return names;
    }

    @Override
    protected void activate(Player player) {
        super.activate(player);
        this.destinations = getDestinations(player);
        if (this.isActive) {
            return;
        }

        this.isActive = true;
    }

    @Override
    protected void deactivate() {
        if (!isOpen() && this.isActive) {
            this.destinations.clear();
            this.destination = null;
            this.isActive = false;
        }
        super.deactivate();
    }


}