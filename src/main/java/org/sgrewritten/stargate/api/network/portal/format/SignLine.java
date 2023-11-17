package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.List;

public interface SignLine {
    List<StargateComponent> getComponents();

    SignLineType getType();
}
