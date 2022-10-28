package net.TheDgtl.Stargate.util.colors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.TheDgtl.Stargate.util.FileHelper;

public class ColorNameInterpreterTest {

    
    @Test
    void hueGetterTest() throws FileNotFoundException, IOException {
        File conversionTestFile = new File("src/test/resources", "colorHueConversionChecks.properties");
        Map<String,String> conversionTests = FileHelper.readKeyValuePairs(FileHelper.getBufferedReader(conversionTestFile));
        for(String key : conversionTests.keySet()) {
            String testString = conversionTests.get(key);
            short expectedValue = Short.valueOf(key);
            short value = ColorNameInterpreter.getHue(testString);
            Assertions.assertEquals(expectedValue,value);
        }
    }

}
