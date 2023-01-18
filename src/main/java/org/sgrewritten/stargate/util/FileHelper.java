package org.sgrewritten.stargate.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.sgrewritten.stargate.Stargate;


/**
 * Utility class for helping with file reading and writing
 */
public final class FileHelper {

    private static final String UTF8_BOM = "\uFEFF";
    private static final int utf8_bom = 0xfeff;
    
    private FileHelper() {

    }

    /**
     * Gets a buffered reader for reading the given file
     *
     * @param file <p>The file to read</p>
     * @return <p>A buffered reader for reading the given file</p>
     * @throws FileNotFoundException <p>If the given file does not exist</p>
     */
    public static BufferedReader getBufferedReader(File file) throws IOException {
        InputStream inputStream = Files.newInputStream(file.toPath());
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText( inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream) );
        charsetDetector.enableInputFilter(true);
        CharsetMatch cm = charsetDetector.detect();
        String encoding =  cm.getName();
        return getBufferedReader(file, encoding);
    }
    
    /**
     * Gets a buffered reader for reading the given file
     * 
     * @param file <p>The file to read</p>
     * @param encoding <p>The encoding of the file </p>
     * @return <p>A buffered reader for reading the given file</p>
     * @throws IOException
     */
    public static BufferedReader getBufferedReader(File file, String encoding) throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), encoding));
    }

    /**
     * Gets a buffered writer for writing to the given file
     *
     * @param file <p>The file to write to</p>
     * @param appendToFile <p>Whether the writer should append to the file</p>
     * @return <p>A buffered writer for writing to the given file</p>
     * @throws FileNotFoundException <p>If the given file does not exist</p>
     */
    public static BufferedWriter getBufferedWriter(File file, boolean appendToFile) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(file, appendToFile);
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
     * Reads key, value pairs in an internal file to the given map
     *
     * @param file      <p>The path of the internal file to read</p>
     * @param targetMap <p>The map to store all found values to</p>
     */
    public static void readInternalFileToMap(String file, Map<String, String> targetMap) {
        Map<String, String> readPairs;
        try {
            InputStream stream = FileHelper.getInputStreamForInternalFile(file);
            if (stream == null) {
                return;
            }
            readPairs = FileHelper.readKeyValuePairs(FileHelper.getBufferedReaderFromInputStream(stream));
            for (String key : readPairs.keySet()) {
                String value = readPairs.get(key);
                if (value.trim().isEmpty()) {
                    targetMap.put(key, null);
                } else {
                    targetMap.put(key, readPairs.get(key));
                }
            }
        } catch (IOException e) {
            Stargate.log(e);
        }
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
        if (string.startsWith(UTF8_BOM)) {
            string = string.substring(1);
        }
        return string;
    }
    
    /**
     * Creates a hidden file if it does not already exist
     * @param dataFolder <p> The stargate datafolder </p>
     * @param internalFolder    <p> The hidden datafolder </p>
     * @param fileName  <p> The name of the file to be created </p>
     * @return  <p> The location of the file created </p>
     * @throws IOException 
     */
    public static File createHiddenFileIfNotExists(String dataFolder, String internalFolder, String fileName) throws IOException {
        File path = new File(dataFolder, internalFolder);
        if (!path.exists() && path.mkdir()) {
            try {
                Files.setAttribute(path.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
        File hiddenFile = new File(path, fileName);
        if(!hiddenFile.exists()) {
            if (!hiddenFile.createNewFile()) {
                throw new FileNotFoundException(fileName + " was not found and could not be created");
            }
        }
        return hiddenFile;
    }

}
