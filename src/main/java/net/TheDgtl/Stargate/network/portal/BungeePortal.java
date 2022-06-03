package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.network.LocalNetwork;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A portal representing a legacy BungeeCord portal
 *
 * <p>This portal type uses several cheats to make the legacy BungeeCord logic work with the new database.</p>
 */
public class BungeePortal extends AbstractPortal {

    private static Network LEGACY_NETWORK;
    private final Network fakeNetwork;
    private final LegacyVirtualPortal targetPortal;
    private final String serverDestination;
    private final String bungeeString;

    static {
        try {
            LEGACY_NETWORK = new LocalNetwork("§§§§§§#BUNGEE#§§§§§§", null, null);
        } catch (NameErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiates a new Bungee Portal
     *
     * @param name              <p>The name of the portal</p>
     * @param destination       <p>The destination of the portal</p>
     * @param destinationServer <p>The destination server to connect to</p>
     * @param flags             <p>The flags enabled for this portal</p>
     * @param ownerUUID         <p>The UUID of this portal's owner</p>
     * @throws NameErrorException <p>If the portal name is invalid</p>
     */
    public BungeePortal(String name, String destination, String destinationServer,
                        Set<PortalFlag> flags, Gate gate, UUID ownerUUID, StargateLogger logger) throws NameErrorException {
        super(getLegacyNetwork(), name, flags, gate, ownerUUID, logger);


        if (destination == null || destination.trim().isEmpty() || destinationServer == null || destinationServer.trim().isEmpty()) {
            throw new NameErrorException(TranslatableMessage.BUNGEE_LACKING_SIGN_INFORMATION);
        }


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
        fakeNetwork = new LocalNetwork(destinationServer, null, null);
        String possibleBungeeString = Stargate.getLanguageManagerStatic().getString(TranslatableMessage.BUNGEE_SIGN_LINE_4);
        bungeeString = (possibleBungeeString == null) ? "[PlaceHolder]" : possibleBungeeString;
    }

    /**
     * Gets the legacy network used for all legacy BungeeCord portals
     *
     * @return <p>The legacy network</p>
     */
    public static Network getLegacyNetwork() {
        return LEGACY_NETWORK;
    }

    @Override
    public void drawControlMechanisms() {
        Stargate.log(Level.FINEST, "serverDestination = " + serverDestination);

        String[] lines = new String[4];
        lines[0] = super.colorDrawer.formatPortalName(this, HighlightingStyle.PORTAL);
        lines[1] = super.colorDrawer.formatPortalName(getDestination(), HighlightingStyle.DESTINATION);
        lines[2] = super.colorDrawer.formatLine(HighlightingStyle.BUNGEE.getHighlightedName(serverDestination));
        lines[3] = super.colorDrawer.formatLine(bungeeString);
        getGate().drawControlMechanisms(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public Portal getDestination() {
        return targetPortal;
    }

    @Override
    public String getDestinationName() {
        return targetPortal.getName();
    }

    @Override
    public Network getNetwork() {
        return fakeNetwork;
    }

}
