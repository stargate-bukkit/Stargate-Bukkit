package net.knarcraft.stargate.utility;

import net.md_5.bungee.api.ChatColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for reading files
 */
public final class FileHelper {

    private FileHelper() {

    }

    /**
     * Gets an input stream from a string pointing to an internal file
     *
     * <p>This is used for getting an input stream for reading a file contained within the compiled .jar file. The file
     * should be in the resources directory, and the file path should start with a forward slash ("/") character.</p>
     *
     * @param file <p>The file to read</p>
     * @return <p>An input stream for the file</p>
     */
    public static InputStream getInputStreamForInternalFile(String file) {
        return FileHelper.class.getResourceAsStream(file);
    }

    /**
     * Gets a buffered reader from a string pointing to a file
     *
     * @param file <p>The file to read</p>
     * @return <p>A buffered reader reading the file</p>
     * @throws FileNotFoundException <p>If the given file does not exist</p>
     */
    public static BufferedReader getBufferedReaderFromString(String file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return getBufferedReaderFromInputStream(fileInputStream);
    }

    /**
     * Gets a buffered reader given an input stream
     *
     * @param inputStream <p>The input stream to read</p>
     * @return <p>A buffered reader reading the input stream</p>
     */
    public static BufferedReader getBufferedReaderFromInputStream(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Gets a buffered writer from a string pointing to a file
     *
     * @param file <p>The file to write to</p>
     * @return <p>A buffered writer writing to the file</p>
     * @throws FileNotFoundException <p>If the file does not exist</p>
     */
    public static BufferedWriter getBufferedWriterFromString(String file) throws FileNotFoundException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamWriter);
    }

    /**
     * Reads key/value pairs from an input stream
     *
     * @param bufferedReader <p>The buffered reader to read</p>
     * @return <p>A map containing the read pairs</p>
     * @throws IOException <p>If unable to read from the stream</p>
     */
    public static Map<String, String> readKeyValuePairs(BufferedReader bufferedReader) throws IOException {
        Map<String, String> readPairs = new HashMap<>();

        String line = bufferedReader.readLine();
        boolean firstLine = true;
        while (line != null) {
            //Strip UTF BOM from the first line
            if (firstLine) {
                line = removeUTF8BOM(line);
                firstLine = false;
            }
            //Split at first "="
            int equalSignIndex = line.indexOf('=');
            if (equalSignIndex == -1) {
                line = bufferedReader.readLine();
                continue;
            }

            //Read the line
            String key = line.substring(0, equalSignIndex);
            String value = ChatColor.translateAlternateColorCodes('&', line.substring(equalSignIndex + 1));
            readPairs.put(key, value);

            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        return readPairs;
    }

    /**
     * Removes the UTF-8 Byte Order Mark if present
     *
     * @param string <p>The string to remove the BOM from</p>
     * @return <p>A string guaranteed without a BOM</p>
     */
    private static String removeUTF8BOM(String string) {
        String UTF8_BOM = "\uFEFF";
        if (string.startsWith(UTF8_BOM)) {
            string = string.substring(1);
        }
        return string;
    }

}
