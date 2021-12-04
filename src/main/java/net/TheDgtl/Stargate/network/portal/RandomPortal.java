package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RandomPortal extends Portal {

    private final Random randomizer = new Random();

    RandomPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID) throws NameErrorException, NoFormatFoundException, GateConflictException {
        super(network, name, sign, flags, ownerUUID);
    }

    @Override
    public void drawControlMechanism() {
        String[] lines = {
                super.colorDrawer.parseName(NameSurround.PORTAL, this),
                super.colorDrawer.parseLine(NameSurround.DESTINATION.getSurround(Stargate.languageManager.getString(TranslatableMessage.RANDOM))),
                super.colorDrawer.parseLine(network.concatName()),
                ""
        };
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
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

