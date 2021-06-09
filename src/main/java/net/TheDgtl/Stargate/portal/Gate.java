package net.TheDgtl.Stargate.portal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;

import net.TheDgtl.Stargate.Stargate;

public class Gate {
	public static List<Gate> gateTypes;

	HashMap<String, GateStructure> portalParts;

	/**
	 * 
	 * @param location
	 * @return
	 */
	public boolean isInPortal(Location location) {
		return portalParts.get("iris").isInPortal(location);
	}

	private static class StargateFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".gate");
		}
	}

	public static List<Gate> loadGates(String gateFolder) {
		List<Gate> allGateTypes = new ArrayList<Gate>();

		File dir = new File(gateFolder);
		File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

		for (File file : files) {
			GateParser gateParser = new GateParser(file);
			try {
				gateParser.open();
				Gate gate = gateParser.parse();
				allGateTypes.add(gate);
			} catch (FileNotFoundException | GateParser.ParsingError e) {
				Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
			} finally {
				gateParser.close();
			}
		}

		return allGateTypes;
	}

	private static class GateParser {
		File file;
		Scanner scanner;
		String line;
		Gate gate;
		HashMap<Character, Material> frameConfig;

		final static char NOTINGATE = ' ';
		final static char EXIT = '*';
		final static char ENTRANCE = '.';

		Material irisOpen;
		Material irisClosed;
		HashMap<String, String> config;

		GateParser(File file) {
			this.file = file;

			frameConfig = new HashMap<Character, Material>();
			// Default settings
			irisOpen = Material.WATER;
			irisClosed = Material.AIR;
		}

		void open() throws FileNotFoundException {
			scanner = new Scanner(file);
		}

		Gate parse() throws ParsingError {
			parseSettings();

			return parseGate();
		}

		private void parseSettings() throws ParsingError {
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (line.isBlank()) {
					scanner.nextLine();
					return;
				}
				if (!line.contains("="))
					return;

				String[] split = line.split("=");
				String key = split[0].trim();
				String value = split[1].trim();

				if (key.length() != 1) {
					switch (key) {
					case "portal-open":
						irisOpen = parseMaterial(value);
						break;
					case "portal-closed":
						irisClosed = parseMaterial(value);
						break;
					default:
						config.put(key, value);
						break;
					}
					continue;
				}

				Character symbol = key.charAt(0);

				Material id = parseMaterial(value);
				frameConfig.put(symbol, id);

			}
		}

		private Material parseMaterial(String stringId) throws ParsingError {
			Material id = Material.getMaterial(stringId);

			if (id == null) {
				throw new ParsingError("Invalid material in line: " + line);
			}
			return id;

		}

		class ParsingError extends Exception {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8103799867513880231L;

			ParsingError(String msg) {
				super(msg);
			}
		}

		private Gate parseGate() throws ParsingError {
			Gate gate = new Gate();
			int cols = 0;
			ArrayList<ArrayList<Character>> design = new ArrayList<>();
			while (scanner.hasNextLine()) {
				ArrayList<Character> row = new ArrayList<>();

				if (line.length() > cols) {
					cols = line.length();
				}

				for (Character symbol : line.toCharArray()) {
					switch (symbol) {
					case NOTINGATE:
						break;
					case EXIT:
						break;
					case ENTRANCE:
						break;
					default:
						if ((symbol.equals('?')) || (!frameConfig.containsKey(symbol))) {
							throw new ParsingError("Unknown symbol '" + symbol + "' in gatedesign");
						}
					}

					row.add(symbol);
				}
			}
		}

		void close() {
			try {
				scanner.close();
			} catch (NullPointerException ex) {
			}
		}
	}
}
