package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PermissionManager;
import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.event.StargateDeactivateEvent;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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

    private final List<IPortal> destinations = new ArrayList<>();
    private static final int ACTIVE_DELAY = 15; // seconds

    public NetworkedPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFound, GateConflict, NameError {
        super(network, name, sign, flags, ownerUUID);
    }

    /**
     * TODO have this individual for each player?
     *
     * @param action
     * @param actor
     */
    @Override
    public void onSignClick(Action action, Player actor) {
        activate(actor);
        if (destinations.size() < 1)
            return;
        if (selectedDestination == NO_DESTINATION_SELECTED || destinations.size() < 2) {
            selectedDestination = getNextDestination(1, -1);
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
            int step = (action == Action.RIGHT_CLICK_BLOCK) ? -1 : 1;
            selectedDestination = getNextDestination(step, selectedDestination);
        }
        drawControlMechanism();
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

    private String[] getDestinations(Player actor) {
        HashSet<String> tempPortalList = network.getAvailablePortals(actor, this);
        return tempPortalList.toArray(new String[0]);
    }

    public IPortal loadDestination() {
        if (selectedDestination == NO_DESTINATION_SELECTED)
            return null;
        return destinations.get(selectedDestination);
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
        this.selectedDestination = NO_DESTINATION_SELECTED;
        deactivate();
    }

    @Override
    public void drawControlMechanism() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.parseName(NameSurround.PORTAL, this);
        if (!isActive) {
            lines[1] = super.colorDrawer.parseLine(Stargate.languageManager.getString(TranslatableMessage.RIGHT_CLICK));
            lines[2] = super.colorDrawer.parseLine(Stargate.languageManager.getString(TranslatableMessage.TO_USE));
            lines[3] = super.colorDrawer.parseLine(network.concatName());
        } else {
            int destinationIndex = selectedDestination % 3;
            int firstDestination = selectedDestination - destinationIndex;
            int maxLength = destinations.size();
            for (int i = 0; i < 3; i++) {
                int destination = i + firstDestination;
                if (destination == maxLength)
                    break;

                if (Setting.getInteger(Setting.NAME_STYLE) == 1) {
                    if (destinationIndex == i) {
                        lines[i + 1] = super.colorDrawer.parseName(NameSurround.DESTINATION, this.getDestination(destination));
                    } else {
                        lines[i + 1] = super.colorDrawer.parseLine(this.getDestination(destination).getName());
                    }
                    continue;
                }

                NameSurround surround;
                if (destinationIndex == i) {
                    surround = NameSurround.DESTINATION;
                } else {
                    surround = NameSurround.NOTHING;
                }
                lines[i + 1] = super.colorDrawer.parseName(surround, this.getDestination(destination));
            }
        }
        getGate().drawControll(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    private void activate(Player actor) {
        /*
         * Schedule for deactivation
         */
        this.activator = actor.getUniqueId();
        long activateTiming = System.currentTimeMillis();
        this.activateTiming = activateTiming;
        PopulatorAction action = new PopulatorAction() {

            @Override
            public void run(boolean forceEnd) {
                deactivate(activateTiming);
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        Stargate.syncSecPopulator.addAction(new DelayedAction(ACTIVE_DELAY, action));

        if (isActive)
            return;

        String[] destinationNames = getDestinations(actor);
        for (String name : destinationNames) {
            destinations.add(network.getPortal(name));
        }
        StargateActivateEvent event = new StargateActivateEvent(this, actor, destinations);
        Bukkit.getPluginManager().callEvent(event);
        PermissionManager permissionManager = new PermissionManager(actor);
        if (event.isCancelled() || !permissionManager.hasPerm(event)) {
            this.destinations.clear();
            selectedDestination = NO_DESTINATION_SELECTED;
        }
        this.isActive = (destinations.size() > 0);
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
        selectedDestination = NO_DESTINATION_SELECTED;
        drawControlMechanism();
    }
}