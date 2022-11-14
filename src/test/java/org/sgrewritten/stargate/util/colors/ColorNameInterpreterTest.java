package org.sgrewritten.stargate.util.colors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ColorNameInterpreterTest {


    @Test
    void hueGetterTest() throws IOException {
        File conversionTestFile = new File("src/test/resources", "colorHueConversionChecks.properties");
        Map<String, String> conversionTests = FileHelper.readKeyValuePairs(FileHelper.getBufferedReader(conversionTestFile));
        for (String key : conversionTests.keySet()) {
            String testString = conversionTests.get(key);
            short expectedValue = Short.parseShort(key);
            System.out.println("testing for testString: " + testString);
            short value = ColorConverter.getHue(ColorNameInterpreter.getColor(testString));
            Assertions.assertEquals(expectedValue, value);
        }
    }

}
