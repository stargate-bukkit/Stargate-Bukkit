package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.event.StargateDeactivateEvent;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NetworkedPortal extends Portal {
    /**
     *
     */
    // used in networked portals
    static final private int NO_DESTINATION_SELECTED = -1;
    private int selectedDestination = NO_DESTINATION_SELECTED;
    private boolean isActive;
    private long activateTiming;
    private UUID activator;

    private List<IPortal> destinations = new ArrayList<>();
    private static final int ACTIVE_DELAY = 15; // seconds

    public NetworkedPortal(Network network, String name, Block sign, Set<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFoundException, GateConflictException, NameErrorException {
        super(network, name, sign, flags, ownerUUID);
    }

    /**
     * TODO have this individual for each player?
     *
     * @param event <p>The event triggering the sign click</p>
     */
    @Override
    public void onSignClick(PlayerInteractEvent event) {
        Player actor = event.getPlayer();
        if ((this.activator != null && !actor.getUniqueId().equals(this.activator)) || this.isOpen())
            return;
        if (!hasActivatePerms(actor)) {
            Stargate.log(Level.CONFIG, "Player did not have permission to activate portal");
            return;
        }
        boolean previuslyActivated = this.isActive;
        activate(actor);
        if (destinations.size() < 1) {
            String message = Stargate.languageManager.getErrorMessage(TranslatableMessage.DESTINATION_EMPTY);
            event.getPlayer().sendMessage(message);
            this.isActive = false;
            return;
        }

        if (!previuslyActivated) {
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
        return;
    }

    @Override
    public void onButtonClick(PlayerInteractEvent event) {
        if (!event.getPlayer().getUniqueId().equals(activator))
            return;
        super.onButtonClick(event);
    }

    private IPortal getDestination(int index) {
        return destinations.get(index);
    }

    private List<IPortal> getDestinations(Player actor) {
        if (actor == null)
            return new ArrayList<>();

        HashSet<String> tempPortalList = network.getAvailablePortals(actor, this);
        tempPortalList.toArray(new String[0]);
        List<IPortal> destinations = new ArrayList<>();
        for (String name : tempPortalList) {
            destinations.add(network.getPortal(name));
        }
        return destinations;
    }

    public IPortal loadDestination() {
        if (selectedDestination == NO_DESTINATION_SELECTED || selectedDestination >= destinations.size())
            return null;
        return destinations.get(selectedDestination);
    }

    /**
     * Check if it's referencing to a destroyed portal, and in that case
     * change what portal it is selecting
     */
    @Override
    public void update() {
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
        /*
         * If the previously selected destination has been removed...
         */
        int aPossibleDestination = getNextDestination(0, this.selectedDestination);
        if (aPossibleDestination == NO_DESTINATION_SELECTED) {
            this.deactivate();
            super.update();
            return;
        }
        selectedDestination = aPossibleDestination;
        super.update();
    }

    /**
     * A method which allows selecting an index x steps away from a reference index
     * without having to bother with index out of bounds stuff. If the index is out
     * of bounds, it will just start counting from 0
     *
     * @param step
     * @param initialDestination
     * @return
     */
    private int getNextDestination(int step, int initialDestination) {
        int destinationLength = destinations.size();
        // Avoid infinite recursion if this is the only gate available
        if (destinationLength < 1) {
            return -1;
        }
        int temp = initialDestination + destinationLength;
        return (temp + step) % destinationLength;
    }

    @Override
    public void close(boolean force) {
        if (hasFlag(PortalFlag.ALWAYS_ON) && !force)
            return;
        super.close(force);
        deactivate();
    }

    @Override
    public void drawControlMechanism() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.compilePortalName(HighlightingStyle.PORTAL, this);
        if (!isActive) {
            lines[1] = super.colorDrawer.compileLine(Stargate.languageManager.getString(TranslatableMessage.RIGHT_CLICK));
            lines[2] = super.colorDrawer.compileLine(Stargate.languageManager.getString(TranslatableMessage.TO_USE));
            lines[3] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.compileLine(network.concatName()) : "";
        } else {
            int destinationIndex = selectedDestination % 3;
            int firstDestination = selectedDestination - destinationIndex;
            int maxLength = destinations.size();
            for (int i = 0; i < 3; i++) {
                int destination = i + firstDestination;
                if (destination == maxLength)
                    break;

                if (Settings.getInteger(Setting.NAME_STYLE) == 1) {
                    if (destinationIndex == i) {
                        lines[i + 1] = super.colorDrawer.compilePortalName(HighlightingStyle.DESTINATION, this.getDestination(destination));
                    } else {
                        lines[i + 1] = super.colorDrawer.compileLine(this.getDestination(destination).getName());
                    }
                    continue;
                }

                HighlightingStyle surround;
                if (destinationIndex == i) {
                    surround = HighlightingStyle.DESTINATION;
                } else {
                    surround = HighlightingStyle.NOTHING;
                }
                lines[i + 1] = super.colorDrawer.compilePortalName(surround, this.getDestination(destination));
            }
        }
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    private boolean hasActivatePerms(Player actor) {
        StargateActivateEvent event = new StargateActivateEvent(this, actor, destinations);
        Bukkit.getPluginManager().callEvent(event);
        PermissionManager permissionManager = new PermissionManager(actor);
        return (!event.isCancelled() && permissionManager.hasPermission(event));
    }

    private void activate(Player actor) {
        /*
         * Schedule for deactivation
         */
        this.activator = actor.getUniqueId();
        long activateTiming = System.currentTimeMillis();
        this.activateTiming = activateTiming;
        Supplier<Boolean> action = () -> {
            deactivate(activateTiming);
            return true;
        };
        Stargate.syncSecPopulator.addAction(new DelayedAction(ACTIVE_DELAY, action));

        if (isActive)
            return;

        this.destinations = getDestinations(actor);
        this.isActive = true;
    }

    /**
     * @param activateTiming the time the portal was activated. Kept track of so that to be sure the deactivation
     *                       and activation matches
     */
    private void deactivate(long activateTiming) {
        if (!isActive || isOpen() || activateTiming != this.activateTiming)
            return;
        deactivate();
    }

    private void deactivate() {
        this.activator = null;
        StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        this.destinations.clear();
        this.isActive = false;
        drawControlMechanism();
    }
}