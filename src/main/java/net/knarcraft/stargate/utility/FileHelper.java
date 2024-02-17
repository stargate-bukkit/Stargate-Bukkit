package net.knarcraft.stargate.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    /**
     * Converts the stream directly into a string, includes the newline character
     *
     * @param stream <p> The stream to read from </p>
     * @return <p> A String of the file read </p>
     * @throws IOException <p>If unable to read the stream</p>
     */
    public static String readStreamToString(InputStream stream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        StringBuilder lines = new StringBuilder();
        while (line != null) {
            lines.append(line).append("\n");
            line = reader.readLine();
        }
        return lines.toString();
    }
}
