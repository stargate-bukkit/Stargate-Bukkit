package org.sgrewritten.stargate.api.network.portal.behavior;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.LegacyVirtualPortal;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.util.NameHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LegacyBungeeBehavior extends AbstractPortalBehavior {
    private final LegacyVirtualPortal targetPortal;
    private final String serverDestination;
    private StargateNetwork fakeNetwork;
    private final String bungeeString;

    public LegacyBungeeBehavior(LanguageManager languageManager, String destination, String destinationServer) throws BungeeNameException {
        super(languageManager);
        destination = NameHelper.getTrimmedName(destination);
        destinationServer = NameHelper.getTrimmedName(destinationServer);

        if (destination == null || destination.isEmpty() || destinationServer == null || destinationServer.isEmpty()) {
            throw new BungeeNameException("Lacking sign information for bungee portal", TranslatableMessage.BUNGEE_LACKING_SIGN_INFORMATION);
        }

        /*
         * CHEATS! we love cheats. This one helps to save the legacy bungee gate into sql table so that the
         * target server is stored as a replacement to network.
         */
        try {
            this.fakeNetwork = new StargateNetwork(destinationServer, NetworkType.CUSTOM, StorageType.LOCAL);
        } catch (InvalidNameException | NameLengthException | UnimplementedFlagException unreachable) {
            Stargate.log(unreachable);
        }
        /*
         * Create a virtual portal that handles everything related
         * to moving the player to a different server. This is set
         * as destination portal, of course.
         *
         * Note that this is only used locally inside this portal
         * and can not be found (should not) in any network anywhere.
         */
        this.targetPortal = new LegacyVirtualPortal(destinationServer, destination, fakeNetwork,
                Set.of(), new HashSet<>(), UUID.randomUUID());
        this.serverDestination = destinationServer;
        String possibleBungeeString = super.languageManager.getString(TranslatableMessage.BUNGEE_SIGN_LINE_4);
        this.bungeeString = (possibleBungeeString == null) ? "[PlaceHolder]" : possibleBungeeString;
    }

    @Override
    public void update() {
        // Nothing needs to be updated for this behavior, everything is static
    }

    @Override
    public @Nullable Portal getDestination() {
        return this.targetPortal;
    }

    @Override
    public @NotNull LineData @NotNull [] getLines() {
        return new LineData[] {
                new PortalLineData(portal, SignLineType.THIS_PORTAL),
                new PortalLineData(targetPortal, SignLineType.DESTINATION_PORTAL),
                new TextLineData(serverDestination, HighlightingStyle.SQUARE_BRACKETS),
                new TextLineData(bungeeString, SignLineType.TEXT)
        };
    }

    @Override
    public @NotNull StargateFlag getAttachedFlag() {
        return StargateFlag.LEGACY_INTERSERVER;
    }
}
