package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RandomPortal extends AbstractPortal {

    private final Random randomizer = new Random();

    public RandomPortal(Network network, String name, Block sign, Set<PortalFlag> flags, UUID ownerUUID) throws NameErrorException, NoFormatFoundException, GateConflictException {
        super(network, name, sign, flags, ownerUUID);
    }

    @Override
    public void drawControlMechanisms() {
        String[] lines = {
                super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL),
                super.colorDrawer.formatLine(HighlightingStyle.DESTINATION.getHighlightedName(Stargate.languageManager.getString(TranslatableMessage.RANDOM))),
                !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.formatLine(network.concatName()) : "",
                ""
        };
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
        String dest = destinations[randomNumber];

        return network.getPortal(dest);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        destination = null;
    }
}

