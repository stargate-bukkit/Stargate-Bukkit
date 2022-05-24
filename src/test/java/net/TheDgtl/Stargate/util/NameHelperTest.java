package net.TheDgtl.Stargate.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class NameHelperTest {

    static final Map<String, String> nameTests = new HashMap<>();

    @BeforeAll
    public static void setUp() {
        nameTests.put(" test     ", "test");
        nameTests.put(" test    test   ", "test test");
        nameTests.put("This is a gate name", "This is a gate name");
        nameTests.put("هذا مثال البوابة", "هذا مثال البوابة");
        nameTests.put("apẹẹrẹ ẹnu-bode", "apẹẹrẹ ẹnu-bode");
        nameTests.put("דעם טויער בייַשפּיל", "דעם טויער בייַשפּיל");
        nameTests.put("بۇ دەرۋازا مىسالى", "بۇ دەرۋازا مىسالى");
        nameTests.put("цей приклад воріт", "цей приклад воріт");
        nameTests.put("Network Name", "Network Name");
        nameTests.put("                    this is a gate name", "this is a gate name");
        nameTests.put("this is a gate name                  ", "this is a gate name");
    }

    @Test
    void test() {
        for (String key : nameTests.keySet()) {
            Assertions.assertEquals(nameTests.get(key), NameHelper.getTrimmedName(key));
        }
    }
}
