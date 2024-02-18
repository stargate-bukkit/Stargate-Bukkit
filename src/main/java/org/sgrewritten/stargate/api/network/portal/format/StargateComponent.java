package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.property.NonLegacyClass;

/**
 * A wrapper class to be able to store both legacy text and {@link Component} text without causing failures when adventure
 * is not shaded.
 */
public class StargateComponent {

    private String legacyText;
    private Component text;

    public StargateComponent(@Nullable String legacyText) {
        this.legacyText = legacyText;
    }

    public StargateComponent(@Nullable Component text) {
        this.text = text;
    }

    /**
     * @return <p>The text text</p>
     */
    public Component getText() {
        if(legacyText != null){
            return LegacyComponentSerializer.legacySection().deserialize(legacyText);
        }
        else if (text != null) {
            return text;
        }
        return Component.empty();
    }

    /**
     * @return <p>The text as legacy format</p>
     */
    public String getLegacyText() {
        if(text != null){
            return LegacyComponentSerializer.legacySection().serialize(text);
        }
        if (legacyText != null) {
            return legacyText;
        }
        return "";
    }

    /**
     * Also overwrites previous set legacy text
     *
     * @param text <p>The text to set</p>
     */
    public void setText(@Nullable Component text) {
        if (legacyText != null) {
            legacyText = null;
        }
        this.text = text;
    }

    /**
     * Also overwrites previous set non-legacy text
     *
     * @param legacyText <p>The legacy text to set</p>
     */
    public void setLegacyText(@Nullable String legacyText) {
        if (text != null) {
            text = null;
        }
        this.legacyText = legacyText;
    }

    @Override
    public String toString(){
        if(this.legacyText == null){
            return MiniMessage.miniMessage().serialize(text);
        }
        return ChatColor.stripColor(legacyText);
    }
}
