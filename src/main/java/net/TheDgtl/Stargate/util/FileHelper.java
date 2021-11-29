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
 * Utility class with only static methods
 */
public class FileHelper {


    /**
     * Gets a buffered reader for reading the given language file
     *
     * @param languageFile <p>The language file to read</p>
     * @return <p>A buffered reader for reading the language file</p>
     * @throws FileNotFoundException <p></p>
     */
    static public BufferedReader getBufferedReader(File languageFile) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(languageFile);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }
    
    static public BufferedWriter getBufferedWriter(File languageFile) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(languageFile);
        OutputStreamWriter outputStreamReader = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamReader);
    }
}
