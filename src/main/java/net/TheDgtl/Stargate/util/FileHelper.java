package net.TheDgtl.Stargate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
    static public BufferedReader getBufferedReader(File file) throws FileNotFoundException {
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
    static public BufferedWriter getBufferedWriter(File file) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter outputStreamReader = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamReader);
    }

}
