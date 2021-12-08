package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.UUID;

public class FixedPortal extends Portal {
    /**
     *
     */
    String destinationName;

    public FixedPortal(Network network, String name, String destinationName, Block sign, Set<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFoundException, GateConflictException, NameErrorException {
        super(network, name, sign, flags, ownerUUID);
        this.destinationName = destinationName;
    }

    @Override
    public void drawControlMechanism() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.parseName(HighlightingStyle.PORTAL, this);
        lines[2] = !this.hasFlag(PortalFlag.HIDE_NETWORK) ? super.colorDrawer.parseLine(this.network.concatName()) : "";
        IPortal destination = loadDestination();
        if (destination != null)
            lines[1] = super.colorDrawer.parseName(HighlightingStyle.DESTINATION, loadDestination());
        else {
            lines[1] = super.colorDrawer.parseLine(destinationName);
            lines[3] = super.colorDrawer.parseError(Stargate.languageManager.getString(TranslatableMessage.DISCONNECTED), HighlightingStyle.BUNGEE);
        }
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
        return this.network.getPortal(destinationName);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        this.openFor = null;
    }
}