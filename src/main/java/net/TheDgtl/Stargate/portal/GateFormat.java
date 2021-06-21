package net.TheDgtl.Stargate.portal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;

public class GateFormat {
	public static List<GateFormat> gateFormats;
	public static HashMap<Material,List<GateFormat>> controlMaterialFormatsMap;
	public HashMap<String, GateStructure> portalParts;
	
	
	private static String CONTROLLKEY = "controll";
	private static String FRAMEKEY = "frame";
	private static String IRISKEY = "iris";
	
	public GateFormat(GateIris iris, GateFrame frame, GateControll controll, HashMap<String, String> config) {
		portalParts = new HashMap<>();
		portalParts.put(IRISKEY, iris);
		portalParts.put(FRAMEKEY, frame);
		portalParts.put(CONTROLLKEY, controll);
	}

	public boolean matches(Gate.VectorOperation converter, Location loc) {

		for (String structKey : portalParts.keySet()) {
			if (!(portalParts.get(structKey).isValidState(converter, loc))) {
				return false;
			}
		}
		return true;
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
	
	public static List<GateFormat> getPossibleGatesFromControll(Material controlBlockId) {
		//TODO
		return gateFormats;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Vector> getControllBlocks() {
		GateControll controll = (GateControll) portalParts.get(CONTROLLKEY);
		
		return (List<Vector>) controll.parts;
	}
	
	private static class GateFormatParser {
		File file;
		Scanner scanner;
		String line;
		HashMap<Character, HashSet<Material>> frameMaterials;

		final static char NOTINGATE = ' ';
		final static char EXIT = '*';
		final static char ENTRANCE = '.';
		final static char CONTROL = '-';

		HashSet<Material> irisOpen;
		HashSet<Material> irisClosed;
		GateIris iris;
		GateFrame frame;
		GateControll control;

		GateFormatParser(File file) {
			this.file = file;

			frameMaterials = new HashMap<>();
			// Default settings
			irisOpen = new HashSet<Material>();
			irisOpen.add(Material.WATER);
			irisClosed = new HashSet<Material>();
			irisClosed.add(Material.AIR);
		}

		void open() throws FileNotFoundException {
			scanner = new Scanner(file);
		}

		void close() {
			try {
				scanner.close();
			} catch (NullPointerException ex) {
			}
		}

		GateFormat parse() throws ParsingError {
			HashMap<String, String> config = parseSettings();
			HashMap<String, String> remainingConfig = setSettings(config);
			
			List<String> designLines = loadDesign();
			setDesign(designLines);

			return new GateFormat(iris, frame, control, remainingConfig);
		}

		
		private HashMap<String, String> setSettings(HashMap<String, String> config) throws ParsingError {
			HashMap<String, String> remaining = new HashMap<>();
			for (String key : config.keySet()) {
				if (key.length() != 1) {
					switch (key) {
					case "portal-open":
						irisOpen = parseMaterial(config.get(key));
						break;
					case "portal-closed":
						irisClosed = parseMaterial(config.get(key));
						break;
					default:
						remaining.put(key, config.get(key));
						break;
					}
					continue;
				}

				Character symbol = key.charAt(0);

				HashSet<Material> id = parseMaterial(config.get(key));
				frameMaterials.put(symbol, id);
			}
			return remaining;
		}

		private HashMap<String, String> parseSettings() throws ParsingError {
			HashMap<String, String> config = new HashMap<>();
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (line.isBlank()) {
					continue;
				}
				if (!line.contains("="))
					break;

				String[] split = line.split("=");
				String key = split[0].trim();
				String value = split[1].trim();
				config.put(key, value);

			}
			return config;
		}

		private static String TAGIDENTIFIER = "#";
		private static String SPLITIDENTIFIER = ",";
		/**
		 * Just parses all of the materials based from tags or material id (when a string starts with #, 
		 * it should be a tag). "," resembles a split in items 
		 * @param stringIdsMsg
		 * @return
		 * @throws ParsingError
		 */
		private HashSet<Material> parseMaterial(String stringIdsMsg) throws ParsingError {
			HashSet<Material> foundIds = new HashSet<>();
			String[] induvidialIDs = stringIdsMsg.split(SPLITIDENTIFIER);
			for(String stringId : induvidialIDs){
				if (stringId.startsWith(TAGIDENTIFIER)) {
					String tagString = stringId.replace(TAGIDENTIFIER, "");
					Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
							NamespacedKey.minecraft(tagString.toLowerCase()), Material.class);
					if (tag == null) {
						throw new ParsingError("Invalid tag in line: " + line);
					}
					foundIds.addAll(tag.getValues());
					continue;
				}
				
				Material id = Material.getMaterial(stringId);
				if (id == null) {
					throw new ParsingError("Invalid material in line: " + line);
				}
				foundIds.add(id);
			}
			return foundIds;

		}

		
		
		private List<String> loadDesign() {
			List<String> designLines = new ArrayList<>();
			/*
			 * The initial line of the gateDesign has been loaded by parseSettings (acting as an endpoint of settings). 
			 * The 1 following line of code takes this into consideration.
			 */
			designLines.add(line);
			while(scanner.hasNextLine()) {
				line = scanner.nextLine();
				designLines.add(line);
			}
			return designLines;
		}
		
		/**
		 * Creates a vector-structure from the character design following this reference system:
		 *     FFF    y 
		 *     C.C    ^ 
		 *     F*F    | 
		 *     FFF    ---->z 
		 *     
		 *     where F,C,.,* resembles a gatedesign and the rest is the coordinate system used by the vectors.
		 *     Note that origo is at the topleft corner of the gatedesign.
		 *     
		 *     Vectors are also divided into 3 different structures:
		 *     - GateIris
		 *     - GateFrame
		 *     - GateControll
		 *     Note that some structures need a selected material
		 * @param lines : The lines in the .gate file with everything about positions (the actual design)
		 * @throws ParsingError
		 */
		private void setDesign(List<String> lines) throws ParsingError {
			iris = new GateIris(irisOpen, irisClosed);
			frame = new GateFrame();
			control = new GateControll();
			for (int lineNr = 0; lineNr < lines.size(); lineNr++) {
				char[] charLine = lines.get(lineNr).toCharArray();
				for (int i = 0; i < charLine.length; i++) {
					Vector selectedLocation = new Vector(0, -lineNr, i);
					setDesignPoint(charLine[i], selectedLocation);
				}
			}
		}
		
		/**
		 * Determines how one char point in the design should be added into the gateStructure
		 * @param key
		 * @param selectedLocation
		 * @throws ParsingError
		 */
		private void setDesignPoint(char key, Vector selectedLocation) throws ParsingError {
			switch (key) {
			case NOTINGATE:
				break;
			case EXIT:
				iris.addExit(selectedLocation);
				break;
			case ENTRANCE:
				iris.addPart(selectedLocation);
				break;
			case CONTROL:
				frame.addPart(selectedLocation, frameMaterials.get(key));
				control.addPart(selectedLocation.add(new Vector(1, 0, 0)));
				break;
			default:
				if ((key == '?') || (!frameMaterials.containsKey(key))) {
					throw new ParsingError("Unknown symbol '" + key + "' in gatedesign");
				}
				frame.addPart(selectedLocation, frameMaterials.get(key));
			}
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
	}
}
