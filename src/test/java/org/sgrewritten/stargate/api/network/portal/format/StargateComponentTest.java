package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class StargateComponentTest {

    private static final String HELLO_WORLD = "Hello world!";
    private static final Component HELLO_WORLD_COMPONENT = Component.text(HELLO_WORLD);
    private static final String COLORED_TEXT = ChatColor.RED + "I'm red!";
    private static final Component COLORED_TEXT_COMPONENT = LegacyComponentSerializer.legacySection().deserialize(COLORED_TEXT);

    @ParameterizedTest
    @MethodSource("getLegacyToComponent")
    void getText_fromLegacy(String legacy, Component component) {
        StargateComponent stargateComponent = new StargateComponent(legacy);
        Assertions.assertEquals(component, stargateComponent.getText());
        Assertions.assertEquals(legacy == null ? "" : legacy, stargateComponent.getLegacyText());
    }

    @ParameterizedTest
    @MethodSource("getComponentToLegacy")
    void getText_fromComponent(Component component, String legacy){
        StargateComponent stargateComponent = new StargateComponent(component);
        Assertions.assertEquals(legacy, stargateComponent.getLegacyText());
        Assertions.assertEquals(component == null ? Component.empty() : component, stargateComponent.getText());
    }

    @ParameterizedTest
    @MethodSource("getComponentToLegacy")
    void setText(Component component, String legacy) {
        StargateComponent stargateComponent = new StargateComponent((Component) null);
        stargateComponent.setText(component);
        Assertions.assertEquals(legacy, stargateComponent.getLegacyText());
        Assertions.assertEquals(component == null ? Component.empty() : component, stargateComponent.getText());
    }

    @ParameterizedTest
    @MethodSource("getLegacyToComponent")
    void setLegacyText(String legacy, Component component) {
        StargateComponent stargateComponent = new StargateComponent((String) null);
        stargateComponent.setLegacyText(legacy);
        Assertions.assertEquals(legacy == null ? "" : legacy, stargateComponent.getLegacyText());
        Assertions.assertEquals(component, stargateComponent.getText());
    }

    private static Stream<Arguments> getLegacyToComponent() {
        return Stream.of(Arguments.of(HELLO_WORLD, HELLO_WORLD_COMPONENT),
                Arguments.of(COLORED_TEXT, COLORED_TEXT_COMPONENT),
                Arguments.of(null, Component.empty()));
    }

    private static Stream<Arguments> getComponentToLegacy() {
        return Stream.of(Arguments.of(HELLO_WORLD_COMPONENT,HELLO_WORLD),
                Arguments.of(COLORED_TEXT_COMPONENT,COLORED_TEXT),
                Arguments.of(null,""));
    }
}