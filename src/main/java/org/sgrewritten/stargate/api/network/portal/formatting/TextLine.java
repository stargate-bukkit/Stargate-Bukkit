package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import org.sgrewritten.stargate.api.container.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * A line which does not relate to any stargate object, it is pure text
 */
public class TextLine extends AbstractSignLine {
    private final SignLineType type;

    public TextLine(List<Holder<StargateComponent>> components, SignLineType type) {
        super(components);
        this.type = type;
    }

    /**
     * Initialize an error line the default way
     *
     * @param style
     * @param text
     * @param textColor
     * @param pointerColor
     */
    public TextLine(HighlightingStyle style, String text, ChatColor textColor, ChatColor pointerColor, SignLineType type) {
        this(new ArrayList<>(), type);
        getComponents().add(LegacyStargateComponent.of(pointerColor + style.getPrefix()));
        getComponents().add(LegacyStargateComponent.of(textColor + text));
        getComponents().add(LegacyStargateComponent.of(pointerColor + style.getSuffix()));
    }

    public TextLine(String text, SignLineType type) {
        this(new ArrayList<>(), type);
        getComponents().add(LegacyStargateComponent.of(text));
    }

    public TextLine(String text) {
        this(new ArrayList<>(), SignLineType.TEXT);
        getComponents().add(LegacyStargateComponent.of(text));
    }

    public TextLine(List<Holder<StargateComponent>> components) {
        this(components, SignLineType.TEXT);
    }

    public TextLine() {
        this(new ArrayList<>(), SignLineType.TEXT);
    }

    @Override
    public SignLineType getType() {
        return this.type;
    }
}
