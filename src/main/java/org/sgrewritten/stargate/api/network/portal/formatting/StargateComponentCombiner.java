package org.sgrewritten.stargate.api.network.portal.formatting;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.container.Holder;

import java.util.List;

public class StargateComponentCombiner {

    private StargateComponentCombiner() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert from SignLine to {@link Component}
     *
     * @param line <p>A sign line to be converted</p>
     * @return <p>A text component</p>
     */
    public static StargateComponent getComponent(@Nullable SignLine line) {
        if (line == null) {
            return StargateComponent.empty();
        }
        StargateComponent output = new EmptyStargateComponent();
        return combine(output, line.getComponents());
    }

    private static StargateComponent combine(StargateComponent starter, List<Holder<StargateComponent>> componentList) {
        StargateComponent output = starter;
        for (Holder<StargateComponent> component : componentList) {
            StargateComponent value = component.value;
            if (value == null) {
                continue;
            }
            output = output.append(value);
        }
        return output;
    }
}
