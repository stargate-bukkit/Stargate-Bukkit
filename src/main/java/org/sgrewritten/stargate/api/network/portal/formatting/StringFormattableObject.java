package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringFormattableObject implements FormattableObject {

    private final String name;
    private final HighlightingStyle highlightingStyle;
    private ChatColor color = null;

    public StringFormattableObject(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
        this.highlightingStyle = HighlightingStyle.NOTHING;
    }

    public StringFormattableObject(@NotNull String name, @NotNull HighlightingStyle highlightingStyle) {
        this.highlightingStyle = Objects.requireNonNull(highlightingStyle);
        this.name = Objects.requireNonNull(name);
    }

    public StringFormattableObject(@NotNull String name, @NotNull HighlightingStyle highlightingStyle, ChatColor color) {
        this(name, highlightingStyle);
        this.color = color;
    }

    @Override
    public @NotNull HighlightingStyle getHighlighting() {
        return highlightingStyle;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable ChatColor getPointerColor(@NotNull Material signMaterial) {
        return null;
    }

    @Override
    public @Nullable ChatColor getOverrideColor() {
        return color;
    }
}
