package net.TheDgtl.Stargate.portal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;

public class GateFormat {
	public static List<GateFormat> gateFormats;
	public HashMap<String, GateStructure> portalParts;

	public GateFormat(GateIris iris, GateFrame frame, HashMap<String, String> config) {
		portalParts.put("iris", iris);
		portalParts.put("frame", frame);
	}

	public boolean matches(HashMap<Vector,Material> comparingStructure) {
		//TODO
		return false;
	}
	
	
	
	private static class StargateFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".gate");
		}
	}

	public static List<GateFormat> loadGateFormats(String gateFolder) {
		List<GateFormat> gateFormats = new ArrayList<GateFormat>();

		File dir = new File(gateFolder);
		File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

		for (File file : files) {
			GateFormatParser gateParser = new GateFormatParser(file);
			try {
				gateParser.open();
				GateFormat gate = gateParser.parse();
				gateFormats.add(gate);
			} catch (FileNotFoundException | GateFormatParser.ParsingError e) {
				Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
			} finally {
				gateParser.close();
			}
		}

		return gateFormats;
	}

	private static class GateFormatParser {
		File file;
		Scanner scanner;
		String line;
		HashMap<Character, Material> frameConfig;

		final static char NOTINGATE = ' ';
		final static char EXIT = '*';
		final static char ENTRANCE = '.';

		Material irisOpen;
		Material irisClosed;
		HashMap<String, String> config;

		GateFormatParser(File file) {
			this.file = file;

			frameConfig = new HashMap<Character, Material>();
			// Default settings
			irisOpen = Material.WATER;
			irisClosed = Material.AIR;
		}

		void open() throws FileNotFoundException {
			scanner = new Scanner(file);
		}

		GateFormat parse() throws ParsingError {
			parseSettings();

			return parseGateDesign();
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

		private GateFormat parseGateDesign() throws ParsingError {
			GateIris iris = new GateIris(irisOpen, irisClosed);
			GateFrame frame = new GateFrame();

			int height = 0;
			do {

				char[] charLine = line.toCharArray();
				for (int i = 0; i < charLine.length; i++) {
					Vector selectedLocation = new Vector(0, -height, i);

					switch (charLine[i]) {
					case NOTINGATE:
						break;
					case EXIT:
						iris.addExit(selectedLocation);
						break;
					case ENTRANCE:
						iris.addPart(selectedLocation);
						break;
					default:
						if ((charLine[i] == '?') || (!frameConfig.containsKey(charLine[i]))) {
							throw new ParsingError("Unknown symbol '" + charLine[i] + "' in gatedesign");
						}
						frame.addPart(selectedLocation, frameConfig.get(charLine[i]));
					}
				}
				height++;
				line = scanner.nextLine();
			} while(scanner.hasNextLine());

			return new GateFormat(iris, frame, config);
		}

		void close() {
			try {
				scanner.close();
			} catch (NullPointerException ex) {
			}
		}
	}
}
