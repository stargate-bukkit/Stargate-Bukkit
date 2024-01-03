package org.sgrewritten.stargate.network.portal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.event.portal.message.AsyncStargateSendMessagePortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.format.*;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.api.event.portal.StargateAccessPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateActivatePortalEvent;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.util.MessageUtils;

import java.util.*;
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
     * @param gate      <p>The gate format used by this portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameLengthException
     */
    public NetworkedPortal(Network network, String name, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, GateAPI gate, UUID ownerUUID,
                           LanguageManager languageManager, StargateEconomyAPI economyAPI, String metaData) throws NameLengthException {
        super(network, name, flags, unrecognisedFlags, gate, ownerUUID, languageManager, economyAPI, metaData);
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

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), super.languageManager);
        if (!hasActivatePermissions(actor, permissionManager)) {
            Stargate.log(Level.CONFIG, "Player did not have permission to activate portal");
            return;
        }

        boolean previouslyActivated = this.isActive;
        activate(actor);
        if (destinations.size() < 1) {
            String message = super.languageManager.getErrorMessage(TranslatableMessage.DESTINATION_EMPTY);
            MessageUtils.sendMessageFromPortal(this,event.getPlayer(),message, MessageType.DESTINATION_EMPTY);

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
        if (!this.isActive || super.activator == null) {
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
        if ((hasFlag(PortalFlag.ALWAYS_ON) && !force) || super.isDestroyed || !super.isOpen()) {
            return;
        }
        super.close(force);
        deactivate();
    }

    @Override
    public SignLine[] getDrawnControlLines() {
        SignLine[] lines = new SignLine[4];
        lines[0] = new PortalLine(super.colorDrawer.formatPortalName(this, HighlightingStyle.MINUS_SIGN),this, SignLineType.THIS_PORTAL);
        if (!this.isActive || this.selectedDestination == NO_DESTINATION_SELECTED) {
            lines[1] = new TextLine(super.colorDrawer.formatLine(super.languageManager.getString(TranslatableMessage.RIGHT_CLICK)));
            lines[2] = new TextLine(super.colorDrawer.formatLine(super.languageManager.getString(TranslatableMessage.TO_USE)));
            lines[3] = new NetworkLine(super.colorDrawer.formatNetworkName(network, network.getHighlightingStyle()),getNetwork());
        } else {
            drawActiveSign(lines);
        }
        return lines;
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
    private void drawActiveSign(SignLine[] lines) {
        int destinationIndex = selectedDestination % 3;
        int firstDestination = selectedDestination - destinationIndex;
        int maxLength = destinations.size();
        for (int lineIndex = 0; lineIndex < 3; lineIndex++) {
            int destination = lineIndex + firstDestination;
            if (destination >= maxLength) {
                lines[lineIndex+1] = new TextLine();
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
    private void drawDestination(int lineIndex, int destination, int destinationIndex, SignLine[] lines) {
        boolean isSelectedPortal = (destinationIndex == lineIndex);
        HighlightingStyle highlightingStyle = isSelectedPortal ? HighlightingStyle.LESSER_GREATER_THAN
                : HighlightingStyle.NOTHING;
        Portal destinationPortal = destinations.get(destination);
        lines[lineIndex + 1] = new PortalLine(super.colorDrawer.formatPortalName(destinationPortal, highlightingStyle),destinationPortal,isSelectedPortal ? SignLineType.DESTINATION_PORTAL : SignLineType.PORTAL);
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
        StargateAccessPortalEvent accessEvent = new StargateAccessPortalEvent(player, this, !hasPermission,
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            String message = null;
            if (accessEvent.getDenyReason() == null) {
                message = super.languageManager.getErrorMessage(TranslatableMessage.ADDON_INTERFERE);
            } else if (!accessEvent.getDenyReason().isEmpty()) {
                message = accessEvent.getDenyReason();
            }
            MessageUtils.sendMessageFromPortal(this,player,message, MessageType.DENY);
            if (hasPermission) {
                Stargate.log(Level.CONFIG, " Access event was denied externally");
            }
            return false;
        }

        //Call the activate event to notify add-ons
        StargateActivatePortalEvent event = new StargateActivatePortalEvent(this, player, getPortalNames(destinations),
                this.getDestinationName());
        Bukkit.getPluginManager().callEvent(event);

        //Update this sign's displayed destinations
        destinations = getPortals(event.getDestinations());
        destination = network.getPortal(event.getDestination());
        getDrawnControlLines();
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
    public void activate(Player player) {
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