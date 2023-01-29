package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FormattableObject {

    public @NotNull HighlightingStyle getHighlighting();

    public @NotNull String getName();

    public @Nullable ChatColor getPointerColor(@NotNull Material signMaterial);

    @Nullable ChatColor getOverrideColor();
}
