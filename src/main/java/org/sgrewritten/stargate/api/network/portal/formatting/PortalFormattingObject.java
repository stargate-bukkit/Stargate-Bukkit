package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.util.colors.ColorProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PortalFormattingObject implements FormattableObject {

    private final Portal portal;
    private final HighlightingStyle highlightingStyle;
    private static final Map<Material, Map<PortalFlag, ChatColor>> flagColors;

    static {
        flagColors = compileFlagColors();
    }


    public PortalFormattingObject(@Nullable Portal portal, @NotNull HighlightingStyle highlightingStyle) {
        this.portal = portal;
        this.highlightingStyle = Objects.requireNonNull(highlightingStyle);
    }

    @Override
    public @NotNull HighlightingStyle getHighlighting() {
        return highlightingStyle;
    }

    @Override
    public @NotNull String getName() {
        return portal == null ? "null" : portal.getName();
    }

    @Override
    public @Nullable ChatColor getPointerColor(@NotNull Material signMaterial) {
        return getFlagColor(portal, signMaterial);
    }

    @Override
    public @Nullable ChatColor getOverrideColor() {
        return null;
    }

    /**
     * Get flag color
     *
     * @param portal <p> The portal to check flags from </p>
     * @return <p> A color corresponding to a portals flag. </p>
     */
    private ChatColor getFlagColor(Portal portal, Material signMaterial) {
        PortalFlag[] flagPriority = new PortalFlag[]{PortalFlag.PRIVATE, PortalFlag.FREE, PortalFlag.HIDDEN,
                PortalFlag.FORCE_SHOW, PortalFlag.BACKWARDS};

        if (portal == null) {
            return null;
        }
        if (portal instanceof VirtualPortal) {
            return flagColors.get(signMaterial).get(PortalFlag.FANCY_INTER_SERVER);
        }
        for (PortalFlag flag : flagPriority) {
            if (portal.hasFlag(flag)) {
                return flagColors.get(signMaterial).get(flag);
            }
        }
        return null;
    }

    /**
     * Compile a map of all the flag-colors, good idea to use, as it avoids having to convert too much between hsb and rgb
     *
     * @return <p> A map of all the flag-colors </p>
     */
    private static Map<Material, Map<PortalFlag, ChatColor>> compileFlagColors() {
        Map<Material, Map<PortalFlag, ChatColor>> flagColors = new HashMap<>();
        for (Material signMaterial : Tag.WALL_SIGNS.getValues()) {
            Map<PortalFlag, ChatColor> materialSpecificFlagColors = new HashMap<>();
            for (PortalFlag key : ColorProperty.getFlagColorHues().keySet()) {
                materialSpecificFlagColors.put(key, ColorProperty.getColorFromHue(signMaterial, ColorProperty.getFlagColorHues().get(key), false));
            }
            flagColors.put(signMaterial, materialSpecificFlagColors);
        }
        return flagColors;
    }
}
