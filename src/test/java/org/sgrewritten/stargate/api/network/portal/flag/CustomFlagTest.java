package org.sgrewritten.stargate.api.network.portal.flag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomFlagTest {

    private CustomFlag flag;

    @BeforeEach
    void setup() {
        this.flag = CustomFlag.getOrCreate('C');
    }

    @AfterEach
    void tearDown() {
        CustomFlag.clear();
    }

    @Test
    void isBehaviorFlag() {
        assertFalse(flag.isBehaviorFlag());
        flag.modify(false, true);
        assertTrue(flag.isBehaviorFlag());
    }

    @Test
    void isInternalFlag() {
        assertFalse(flag.isInternalFlag());
        flag.modify(true, false);
        assertTrue(flag.isInternalFlag());
    }

    @Test
    void modify_twice() {
        flag.modify(false, false);
        assertThrows(IllegalStateException.class, () -> flag.modify(true, true));
    }

    @Test
    void getCharacterRepresentation() {
        assertEquals('C', flag.getCharacterRepresentation());
    }

    @Test
    void testEquals_sameIsEquals() {
        // A really stupid test, but who knows what could happen when you modify he equals method
        assertEquals(flag,flag);
    }

    @ParameterizedTest
    @ValueSource(chars = {'A', 'a'})
    void getOrCreate_clashingWithCoreFlag(char flagChar) {
        assertThrows(IllegalArgumentException.class, () -> CustomFlag.getOrCreate(flagChar));
    }

    @Test
    void getOrCreate_lowerCase() {
        assertThrows(IllegalArgumentException.class, () -> CustomFlag.getOrCreate('g'));
    }
}