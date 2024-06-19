package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import org.sgrewritten.stargate.api.container.Holder;
import java.util.ArrayList;
import java.util.List;

public class NetworkLine extends AbstractSignLine {


    private final Network network;

    public NetworkLine(List<Holder<StargateComponent>> components, Network network) {
        super(new ArrayList<>(components));
        this.network = network;
    }

    public NetworkLine(HighlightingStyle style, Network network, ChatColor textColor, ChatColor pointerColor) {
        super(new ArrayList<>());
        List<Holder<StargateComponent>> components = getComponents();
        components.add(LegacyStargateComponent.of(pointerColor + style.getPrefix()));
        components.add(LegacyStargateComponent.of(textColor + (network == null ? "null" : network.getName())));
        components.add(LegacyStargateComponent.of(pointerColor + style.getSuffix()));
        this.network = network;
    }

    /**
     * @return <p>The network this line relates to</p>
     */
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public SignLineType getType() {
        return SignLineType.NETWORK;
    }
}
