package org.sgrewritten.stargate.api.network.portal.formatting.data;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;

import java.util.Objects;

public class NetworkLineData implements LineData {

    private final Network network;

    public NetworkLineData(@NotNull Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public @NotNull Network getNetwork() {
        return network;
    }

    @Override
    public @NotNull SignLineType getType() {
        return SignLineType.NETWORK;
    }

    @Override
    public @NotNull String getText() {
        return network.getName();
    }

    @Override
    public String toString(){
        return "NetworkLineData(" + getText() + ")";
    }
}
