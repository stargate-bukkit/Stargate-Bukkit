package net.TheDgtl.Stargate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
 * Utility class for helping with file reading and writing
 */
public class FileHelper {

    /**
     * Gets a buffered reader for reading the given file
     *
     * @param file <p>The file to read</p>
     * @return <p>A buffered reader for reading the given file</p>
     * @throws FileNotFoundException <p>If the given file does not exist</p>
     */
    public static BufferedReader getBufferedReader(File file) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Gets a buffered writer for writing to the given file
     *
     * @param file <p>The file to write to</p>
     * @return <p>A buffered writer for writing to the given file</p>
     * @throws FileNotFoundException <p>If the given file does not exist</p>
     */
    public static BufferedWriter getBufferedWriter(File file) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter outputStreamReader = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamReader);
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
     * Reads key/value pairs from an input stream
     *
     * @param bufferedReader <p>The buffered reader to read</p>
     * @return <p>A map containing the read pairs</p>
     * @throws IOException <p>If unable to read from the stream</p>
     */
    public static Map<String, String> readKeyValuePairs(BufferedReader bufferedReader) throws IOException {
        Map<String, String> readPairs = new HashMap<>();

        String line = removeUTF8BOM(bufferedReader.readLine());
        while (line != null) {
            //Skip comment lines
            if (line.startsWith("#")) {
                line = bufferedReader.readLine();
                continue;
            }

            //Split at first "="
            int equalSignIndex = line.indexOf('=');
            if (equalSignIndex == -1) {
                line = bufferedReader.readLine();
                continue;
            }

            //Read the line
            String key = line.substring(0, equalSignIndex);
            String value = line.substring(equalSignIndex + 1);
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
    public static String removeUTF8BOM(String string) {
        String UTF8_BOM = "\uFEFF";
        if (string.startsWith(UTF8_BOM)) {
            string = string.substring(1);
        }
        return string;
    }

}
