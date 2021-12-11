package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.event.StargateDeactivateEvent;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A portal that is part of a network
 *
 * <p>For a networked portal, all portals in its network are its destinations</p>
 */
public class NetworkedPortal extends Portal {

    private static final int NO_DESTINATION_SELECTED = -1;
    private static final int ACTIVE_DELAY = 15;

    private int selectedDestination = NO_DESTINATION_SELECTED;
    private List<IPortal> destinations = new ArrayList<>();
    private boolean isActive;
    private long activatedTime;
    private UUID activator;

    /**
     * Instantiates a new networked portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param signBlock <p>The block this portal's sign is located at</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException     <p>If the portal name is invalid</p>
     * @throws NoFormatFoundException <p>If no gate format matches the portal</p>
     * @throws GateConflictException  <p>If the portal's gate conflicts with an existing one</p>
     */
    public NetworkedPortal(Network network, String name, Block signBlock, Set<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFoundException, GateConflictException, NameErrorException {
        super(network, name, signBlock, flags, ownerUUID);
    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {
        //TODO have this individual for each player?

        Player actor = event.getPlayer();
        if ((this.activator != null && !actor.getUniqueId().equals(this.activator)) || this.isOpen()) {
            return;
        }
        if (!hasActivatePermissions(actor)) {
            Stargate.log(Level.CONFIG, "Player did not have permission to activate portal");
            return;
        }
        boolean previouslyActivated = this.isActive;
        activate(actor);
        if (destinations.size() < 1) {
            String message = Stargate.languageManager.getErrorMessage(TranslatableMessage.DESTINATION_EMPTY);
            event.getPlayer().sendMessage(message);
            this.isActive = false;
            return;
        }

        if (!previouslyActivated) {
            if (!Settings.getBoolean(Setting.REMEMBER_LAST_DESTINATION))
                selectedDestination = 0;
            drawControlMechanism();
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            int step = (event.getAction() == Action.RIGHT_CLICK_BLOCK) ? 1 : -1;
            selectedDestination = getNextDestination(step, selectedDestination);
        }
        drawControlMechanism();
    }

    @Override
    public void onButtonClick(PlayerInteractEvent event) {
        if (!event.getPlayer().getUniqueId().equals(activator)) {
            return;
        }
        super.onButtonClick(event);
    }

    @Override
    public void update() {
        //Check if it's referencing to a destroyed portal, and in that case, change what portal it is selecting
        if (!isActive) {
            return;
        }
        IPortal destination = this.destinations.get(this.selectedDestination);
        destinations = getDestinations(Bukkit.getPlayer(activator));
        if (destinations.contains(destination)) {
            this.selectedDestination = destinations.indexOf(destination);
            super.update();
            return;
        }
        //If the previously selected destination has been removed...
        int possibleDestination = getNextDestination(0, this.selectedDestination);
        if (possibleDestination == NO_DESTINATION_SELECTED) {
            this.deactivate();
            super.update();
            return;
        }
        selectedDestination = possibleDestination;
        super.update();
    }

    @Override
    public void close(boolean force) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !force) {
            return;
        }
        super.close(force);
        deactivate();
    }

    @Override
    public void drawControlMechanism() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        if (!isActive) {
            lines[1] = super.colorDrawer.formatLine(Stargate.languageManager.getString(TranslatableMessage.RIGHT_CLICK));
            lines[2] = super.colorDrawer.formatLine(Stargate.languageManager.getString(TranslatableMessage.TO_USE));
            lines[3] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatLine(network.concatName()) : "";
        } else {
            drawActiveSign(lines);
        }
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
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
        if (Settings.getInteger(Setting.NAME_STYLE) == 1) {
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
    private List<IPortal> getDestinations(Player player) {
        if (player == null) {
            return new ArrayList<>();
        }

        Set<String> availablePortals = network.getAvailablePortals(player, this);
        availablePortals.toArray(new String[0]);
        List<IPortal> destinations = new ArrayList<>();
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
    private boolean hasActivatePermissions(Player player) {
        StargateActivateEvent event = new StargateActivateEvent(this, player, destinations);
        Bukkit.getPluginManager().callEvent(event);
        PermissionManager permissionManager = new PermissionManager(player);
        return (!event.isCancelled() && permissionManager.hasPermission(event));
    }

    /**
     * Activates this portal for the given player
     *
     * @param player <p>The player to activate this portal for</p>
     */
    private void activate(Player player) {
        this.activator = player.getUniqueId();
        long activationTime = System.currentTimeMillis();
        this.activatedTime = activationTime;

        //Schedule for deactivation
        Stargate.syncSecPopulator.addAction(new DelayedAction(ACTIVE_DELAY, () -> {
            deactivate(activationTime);
            return true;
        }));

        if (isActive) {
            return;
        }

        this.destinations = getDestinations(player);
        this.isActive = true;
    }

    /**
     * De-activates this portal if necessary
     *
     * <p>The activated time must match to make sure to skip de-activation requests except for the one cancelling the
     * newest portal activation.</p>
     *
     * @param activatedTime <p>The time this portal was activated</p>
     */
    private void deactivate(long activatedTime) {
        if (!isActive || isOpen() || activatedTime != this.activatedTime) {
            return;
        }
        deactivate();
    }

    /**
     * De-activates this portal
     */
    private void deactivate() {
        this.activator = null;
        StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        this.destinations.clear();
        this.isActive = false;
        drawControlMechanism();
    }

}