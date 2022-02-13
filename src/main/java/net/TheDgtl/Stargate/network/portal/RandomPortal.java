package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.NetworkAPI;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * A portal that always chooses a random destination within its own network
 */
public class RandomPortal extends AbstractPortal {

    private final Random randomizer = new Random();

    /**
     * Instantiates a new random portal
     *
     * @param network   <p>The network the portal belongs to</p>
     * @param name      <p>The name of the portal</p>
     * @param flags     <p>The flags enabled for the portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    public RandomPortal(NetworkAPI network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, StargateLogger logger)
            throws NameErrorException {
        super(network, name, flags, gate, ownerUUID, logger);
    }

    @Override
    public void drawControlMechanisms() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        lines[1] = super.colorDrawer.formatLine(HighlightingStyle.DESTINATION.getHighlightedName(
                Stargate.languageManager.getString(TranslatableMessage.RANDOM)));
        lines[2] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatLine(network.getHighlightedName()) : "";
        lines[3] = "";
        getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public Portal loadDestination() {
        Set<String> allPortalNames = network.getAvailablePortals(null, this);
        String[] destinations = allPortalNames.toArray(new String[0]);
        if (destinations.length < 1) {
            return null;
        }
        int randomNumber = randomizer.nextInt(destinations.length);
        String destination = destinations[randomNumber];
        return network.getPortal(destination);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        destination = null;
    }

}

