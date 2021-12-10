package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A portal representing a legacy BungeeCord portal
 *
 * <p>This portal type uses several cheats to make the legacy BungeeCord logic work with the new database.</p>
 */
public class BungeePortal extends Portal {

    private static Network LEGACY_NETWORK;
    private final Network cheatNetwork;
    private final LegacyVirtualPortal targetPortal;
    private final String serverDestination;

    static {
        try {
            LEGACY_NETWORK = new Network("§§§§§§#BUNGEE#§§§§§§", null, null);
        } catch (NameErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiates a new Bungee Portal
     *
     * @param network           <p>The network the portal belongs to</p>
     * @param name              <p>The name of the portal</p>
     * @param destination       <p>The destination of the portal</p>
     * @param destinationServer <p>The destination server to connect to</p>
     * @param signBlock         <p>The block this portal's sign is located at</p>
     * @param flags             <p>The flags enabled for this portal</p>
     * @param ownerUUID         <p>The UUID of this portal's owner</p>
     * @throws NameErrorException     <p>If the portal name is invalid</p>
     * @throws NoFormatFoundException <p>If no gate format matches the portal</p>
     * @throws GateConflictException  <p>If the portal's gate conflicts with an existing one</p>
     */
    public BungeePortal(Network network, String name, String destination, String destinationServer, Block signBlock,
                        Set<PortalFlag> flags, UUID ownerUUID) throws NameErrorException, NoFormatFoundException,
            GateConflictException {
        super(network, name, signBlock, flags, ownerUUID);

        /*
         * Create a virtual portal that handles everything related
         * to moving the player to a different server. This is set
         * as destination portal, of course.
         *
         * Note that this is only used locally inside this portal
         * and can not be found (should not) in any network anywhere.
         */
        targetPortal = new LegacyVirtualPortal(this, destinationServer, destination, LEGACY_NETWORK,
                EnumSet.noneOf(PortalFlag.class), ownerUUID);
        this.serverDestination = destinationServer;
        /*
         * CHEATS! we love cheats. This one helps to save the legacy bungee gate into sql table so that the
         * target server is stored as a replacement to network.
         */
        cheatNetwork = new Network(destinationServer, null, null);
    }

    @Override
    public void drawControlMechanism() {
        Stargate.log(Level.FINEST, "serverDestination = " + serverDestination);

        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        lines[1] = super.colorDrawer.formatPortalName(loadDestination(), HighlightingStyle.DESTINATION);
        lines[2] = super.colorDrawer.formatLine(serverDestination);
        lines[3] = "";
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
        return targetPortal;
    }

    @Override
    public Network getNetwork() {
        return cheatNetwork;
    }

}
