package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.Network;

public class NetworkFormattingObject implements FormattableObject {

    private final Network network;
    private final HighlightingStyle highlighting;
    private final boolean hideNetwork;

    public NetworkFormattingObject(@Nullable Network network, boolean hideNetwork) {
        this.network = network;
        this.hideNetwork = hideNetwork;
        this.highlighting = network == null ? HighlightingStyle.NOTHING : network.getHighlightingStyle();
    }

    @Override
    public @NotNull HighlightingStyle getHighlighting() {
        return highlighting;
    }

    @Override
    public @NotNull String getName() {
        if (hideNetwork) {
            return "";
        }
        return network == null ? "null" : network.getName();
    }

    @Override
    public @Nullable ChatColor getPointerColor(@NotNull Material signMaterial) {
        return null;
    }

    @Override
    public @Nullable ChatColor getOverrideColor() {
        return null;
    }
}
