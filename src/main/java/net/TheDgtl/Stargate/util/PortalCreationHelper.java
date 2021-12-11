package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.FixedPortal;
import net.TheDgtl.Stargate.network.portal.NetworkedPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RandomPortal;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.UUID;

public class PortalCreationHelper {

    public static RealPortal createPortalFromSign(Network net, String[] lines, Block block, Set<PortalFlag> flags, UUID ownerUUID)
            throws NameErrorException, NoFormatFoundException, GateConflictException {
        if (flags.contains(PortalFlag.BUNGEE))
            return new BungeePortal(net, lines[0], lines[1], lines[2], block, flags, ownerUUID);
        if (flags.contains(PortalFlag.RANDOM))
            return new RandomPortal(net, lines[0], block, flags, ownerUUID);
        if (flags.contains(PortalFlag.NETWORKED))
            return new NetworkedPortal(net, lines[0], block, flags, ownerUUID);
        return new FixedPortal(net, lines[0], lines[1], block, flags, ownerUUID);
    }
}
