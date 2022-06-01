package net.TheDgtl.Stargate.network.portal.formatting;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.portal.Portal;

public class LegacyLineColorFormatter extends AbstractLineColorFormatter{

   
    
    
    public LegacyLineColorFormatter(Material signMaterial) {
        super(signMaterial);
    }

    @Override
    public String formatPortalName(Portal portal, HighlightingStyle highlightingStyle) {
        ChatColor color = getColor();
        return color + highlightingStyle.getHighlightedName((portal != null) ? portal.getName() : "null");
    }

    @Override
    public String formatLine(String line) {
        return getColor() + line;
    }

    @Override
    public String formatErrorLine(String error, HighlightingStyle highlightingStyle) {
        return getColor() + highlightingStyle.getHighlightedName(error);
    }
    
    private ChatColor getColor() {
        return super.isLightSign ? Stargate.legacyDefaultLightSignColor : Stargate.legacyDefaultDarkSignColor;
    }

}
