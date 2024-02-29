package org.sgrewritten.stargate.api.network.portal.formatting;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StargateComponentDeserialiser {

    private StargateComponentDeserialiser(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert from SignLine to {@link Component}
     *
     * @param line <p>A sign line to be converted</p>
     * @return <p>A text component</p>
     */
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

    /**
     * Convert from SignLine to legacy text
     *
     * @param line <p>A sign line</p>
     * @return <p>Legacy text</p>
     */
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
