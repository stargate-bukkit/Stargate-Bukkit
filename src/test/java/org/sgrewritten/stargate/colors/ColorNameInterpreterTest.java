package org.sgrewritten.stargate.colors;

import net.md_5.bungee.api.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ColorNameInterpreterTest {


    @ParameterizedTest
    @MethodSource("getExpectedHues")
    void hueGetterTest(TwoTuple<String, String> twoTuple) throws IOException {
        String expected = twoTuple.getFirstValue();
        String testString = twoTuple.getSecondValue();
        short expectedValue = Short.parseShort(expected);
        Stargate.log(Level.FINER, "testing for testString: " + testString);
        short value = ColorConverter.getHue(ColorNameInterpreter.getColor(testString));
        Assertions.assertEquals(expectedValue, value);
    }

    @Test
    void dyeColorInterpretTextColorTest() {
        ChatColor expected = ChatColor.of("#cfbc9d");
        ChatColor value = ColorNameInterpreter.getDefaultTextColor("BROWN");
        Assertions.assertEquals(expected, value);
    }

    @Test
    void dyeColorInterpretPointerColorTest() {
        ChatColor expected = ChatColor.of("#ce5204");
        ChatColor value = ColorNameInterpreter.getDefaultPointerColor("BROWN");
        Assertions.assertEquals(expected, value);
    }

    @Test
    void customTextColorNotNull() {
        Assertions.assertNotNull(ColorNameInterpreter.getDefaultTextColor("#000000"));
    }

    @Test
    void customPointerColorNotNull() {
        Assertions.assertNotNull(ColorNameInterpreter.getDefaultPointerColor("#000000"));
    }

    private static Stream<TwoTuple<String, String>> getExpectedHues() throws IOException {
        List<TwoTuple<String, String>> output = new ArrayList<>();
        File conversionTestFile = new File("src/test/resources", "colors/colorHueConversionChecks.properties");
        Map<String, String> conversionTests = FileHelper.readKeyValuePairs(FileHelper.getBufferedReader(conversionTestFile));
        for (String key : conversionTests.keySet()) {
            output.add(new TwoTuple<>(key, conversionTests.get(key)));
        }
        return output.stream();
    }
}
