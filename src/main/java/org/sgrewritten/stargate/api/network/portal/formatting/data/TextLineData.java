package org.sgrewritten.stargate.api.network.portal.formatting.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

public class TextLineData implements LineData {

    private final String text;
    private final SignLineType type;
    private HighlightingStyle highlightingStyle = null;

    public TextLineData(){
        this.text = "";
        this.type = SignLineType.TEXT;
    }

    public TextLineData(String text, SignLineType type){
        this.text = text;
        this.type = type;
    }

    public TextLineData(String text, HighlightingStyle highlightingStyle) {
        this.text = text;
        this.type = SignLineType.TEXT;
        this.highlightingStyle = highlightingStyle;
    }

    @Override
    public @NotNull SignLineType getType() {
        return this.type;
    }

    @Override
    public @NotNull String getText() {
        return this.text;
    }

    public @Nullable HighlightingStyle getHighlightingStyle(){
        return this.highlightingStyle;
    }
}
