package org.sgrewritten.stargate.network.portal;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.*;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

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
     * @param gate      <p>The gate format used by this portal</p>
     * @param ownerUUID <p>The UUID of the portal's owner</p>
     * @throws NameLengthException
     */
    public RandomPortal(Network network, String name, Set<PortalFlag> flags, Gate gate, UUID ownerUUID, LanguageManager languageManager, StargateEconomyAPI economyAPI)
            throws NameLengthException {
        super(network, name, flags, gate, ownerUUID, languageManager, economyAPI);
    }

    @Override
    public void drawControlMechanisms() {
        GateTextDisplayHandler display = super.getPortalTextDisplay();
        if (display == null) {
            return;
        }
        FormattableObject[] lines = new FormattableObject[4];
        lines[0] = new PortalFormattingObject(this, HighlightingStyle.MINUS_SIGN);
        lines[1] = new StringFormattableObject(
                super.languageManager.getString(TranslatableMessage.RANDOM), HighlightingStyle.LESSER_GREATER_THAN);
        lines[2] = new NetworkFormattingObject(network, this.hasFlag(PortalFlag.HIDE_NETWORK));
        lines[3] = new StringFormattableObject("");
        display.displayText(lines, this);
    }

    @Override
    public Portal getDestination() {
        Set<String> allPortalNames = network.getAvailablePortals(null, this);
        String[] destinations = allPortalNames.toArray(new String[0]);
        if (destinations.length < 1) {
            return null;
        }
        int randomNumber = randomizer.nextInt(destinations.length);
        String destination = destinations[randomNumber];
        Stargate.log(Level.FINEST, String.format("Chose random destination %s, calculated from integer %d", destination, randomNumber));
        return network.getPortal(destination);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        destination = null;
    }

    @Override
    public void doTeleport(Entity target) {
        if (hasFlag(PortalFlag.ALWAYS_ON)) {
            super.destination = getDestination();
        }
        super.doTeleport(target);
    }

}

