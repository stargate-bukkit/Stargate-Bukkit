package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RandomPortal extends Portal {

    private final Random randomizer = new Random();

    RandomPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID) throws NameError, NoFormatFound, GateConflict {
        super(network, name, sign, flags, ownerUUID);
    }

    @Override
    public void onSignClick(Action action, Player actor) {
    }

    @Override
    public void drawControll() {
        String[] lines = {
                IPortal.getDefaultColor(super.isLightSign()) + NameSurround.PORTAL.getSurround(getColoredName(super.isLightSign())),
                IPortal.getDefaultColor(super.isLightSign()) + NameSurround.DESTI.getSurround(Stargate.langManager.getString(LangMsg.RANDOM)),
                IPortal.getDefaultColor(super.isLightSign()) + network.concatName(),
                ""
        };
        getGate().drawControll(lines, !hasFlag(PortalFlag.ALWAYS_ON));
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

