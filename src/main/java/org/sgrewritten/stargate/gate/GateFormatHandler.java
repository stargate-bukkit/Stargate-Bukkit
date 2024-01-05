package org.sgrewritten.stargate.gate;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.ParsingErrorException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * A handler that keeps track of all known gate formats
 */
public class GateFormatHandler {

    /**
     * Loads all gate formats from the gate folder
     *
     * @param dir <p>The folder to load gates from</p>
     * @return <p>A map between a control block material and the corresponding gate format</p>
     */
    public static List<GateFormat> loadGateFormats(File dir) {
        Stargate.log(Level.FINE, "Loading gates from " + dir.getAbsolutePath());
        List<GateFormat> gateFormatMap = new ArrayList<>();
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];

        if (files == null) {
            return null;
        }

        for (File file : files) {
            try {
                gateFormatMap.add(loadGateFormat(file));
            } catch (FileNotFoundException | ParsingErrorException exception) {
                Stargate.log(Level.WARNING, "Could not load Gate " + file.getName() + " - " + exception.getMessage());
                if (exception instanceof ParsingErrorException && file.exists()) {
                    if (!file.renameTo(new File(dir, file.getName() + ".invalid"))) {
                        Stargate.log(Level.WARNING, "Could not add .invalid to gate. Make sure file " +
                                "permissions are set correctly.");
                    }
                }
            }
        }
        return gateFormatMap;
    }

    /**
     * Loads the given gate format file
     *
     * @param file <p>The gate format file to load</p>
     * @throws ParsingErrorException <p>If unable to load the gate format</p>
     * @throws FileNotFoundException <p>If the gate file does not exist</p>
     */
    private static GateFormat loadGateFormat(File file) throws ParsingErrorException,
            FileNotFoundException {
        Stargate.log(Level.CONFIG, "Loaded gate format " + file.getName());
        try (Scanner scanner = new Scanner(file)) {
            Stargate.log(Level.FINER, "Gate file size:" + file.length());
            if (file.length() > 65536L) {
                throw new ParsingErrorException("Design is too large");
            }

            GateFormatParser gateParser = new GateFormatParser(scanner, file.getName());
            return gateParser.parseGateFormat();
        }
    }

}
