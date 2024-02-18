package org.sgrewritten.stargate.api.network.portal.format;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StargateComponentTest {

    private static final String HELLO_1 = "Hello 1";
    private static final String HELLO_2 = "Hello 2";
    private StargateComponent stargateComponent;
    private StargateComponent stargateComponentLegacy;
    private StargateComponent stargateComponentNull;


    @BeforeEach
    void setUp() {
        this.stargateComponent = new StargateComponent(Component.text(HELLO_1));
        this.stargateComponentLegacy = new StargateComponent(HELLO_2);
        this.stargateComponentNull = new StargateComponent((String) null);
    }

    @Test
    void getText() {
        Assertions.assertEquals(Component.text(HELLO_1), stargateComponent.getText());
        Assertions.assertEquals(Component.text(HELLO_2), stargateComponentLegacy.getText());
        Assertions.assertEquals(Component.empty(), stargateComponentNull.getText());
    }

    private static Stream<Arguments> getTextTestArguments(){
        
    }

    @Test
    void getLegacyText() {
        Assertions.assertEquals(HELLO_1, stargateComponent.getLegacyText());
        Assertions.assertEquals(HELLO_2, stargateComponentLegacy.getLegacyText());
        Assertions.assertEquals("", stargateComponentNull.getLegacyText());
    }

    @Test
    void setText() {

    }

    @Test
    void setLegacyText() {
    }

    @Test
    void testToString() {
    }
}