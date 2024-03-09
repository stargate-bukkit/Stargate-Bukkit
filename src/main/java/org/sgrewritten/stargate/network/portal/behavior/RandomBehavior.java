package org.sgrewritten.stargate.network.portal.behavior;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.NetworkLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.PortalLineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

public class RandomBehavior extends AbstractPortalBehavior {
    private static final Random RANDOM = new Random();

    public RandomBehavior(LanguageManager languageManager) {
        super(languageManager);
    }

    @Override
    public void update() {

    }

    @Override
    public @Nullable Portal getDestination() {
        Set<String> allPortalNames = portal.getNetwork().getAvailablePortals(null, portal);
        String[] destinations = allPortalNames.toArray(new String[0]);
        if (destinations.length < 1) {
            return null;
        }
        int randomNumber = RANDOM.nextInt(destinations.length);
        String destination = destinations[randomNumber];
        Stargate.log(Level.FINEST, String.format("Chose random destination %s, calculated from integer %d", destination, randomNumber));
        return portal.getNetwork().getPortal(destination);
    }

    @Override
    public @NotNull LineData @NotNull [] getLines() {
        return new LineData[]{
                new PortalLineData(portal, SignLineType.THIS_PORTAL),
                new TextLineData(super.languageManager.getString(TranslatableMessage.RANDOM), HighlightingStyle.LESSER_GREATER_THAN),
                new NetworkLineData(portal.getNetwork()),
                new TextLineData()};
    }

    @Override
    public @NotNull StargateFlag getAttachedFlag() {
        return StargateFlag.RANDOM;
    }
}
