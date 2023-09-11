package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class StargateComponent {

    private String legacyText;
    private Component text;

    public StargateComponent(String legacyText){
        this.legacyText = legacyText;
    }

    public StargateComponent(Component text){
        this.text = text;
    }

    public Component getText(){
        if(text == null){
            return LegacyComponentSerializer.legacySection().deserialize(legacyText);
        }
        return text;
    }

    public String getLegacyText(){
        if(legacyText == null){
            return LegacyComponentSerializer.legacySection().serialize(text);
        }
        return legacyText;
    }
}
