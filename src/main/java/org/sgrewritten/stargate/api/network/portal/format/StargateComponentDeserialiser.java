package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StargateComponentDeserialiser {

    public static Component getComponent(@Nullable SignLine line) {
        if (line == null) {
            return Component.empty();
        }
        List<StargateComponent> componentList = line.getComponents();
        Component output = Component.empty();
        for (StargateComponent component : componentList) {
            output = output.append(component.getText());
        }
        return output;
    }

    public static String getLegacyText(SignLine line) {
        if (line == null) {
            return "";
        }
        List<StargateComponent> componentList = line.getComponents();
        StringBuilder builder = new StringBuilder();
        for (StargateComponent component : componentList) {
            builder.append(component.getLegacyText());
        }
        return builder.toString();
    }
}
