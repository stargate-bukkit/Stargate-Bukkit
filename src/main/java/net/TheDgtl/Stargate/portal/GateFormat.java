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
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;

public class GateFormat {
	public static HashMap<Material,List<GateFormat>> controlMaterialFormatsMap;
	public HashMap<String, GateStructure> portalParts;
	
	public final String name;
	public static final String CONTROLLKEY = "controll";
	public static final String FRAMEKEY = "frame";
	public static final String IRISKEY = "iris";
	public GateFormat(GateIris iris, GateFrame frame, GateControll controll, HashMap<String, String> config,
			String name) {
		portalParts = new HashMap<>();
		portalParts.put(IRISKEY, iris);
		portalParts.put(FRAMEKEY, frame);
		portalParts.put(CONTROLLKEY, controll);
		this.name = name;
	}

	/**
	 * Checks through every structure in the format, and checks whether they are valid
	 * @param converter
	 * @param loc
	 * @return true if all structures are valid
	 */
	public boolean matches(Gate.VectorOperation converter, Location loc) {
		for (String structKey : portalParts.keySet()) {
			Stargate.log(Level.FINER, "---Validating " + structKey);
			if (!(portalParts.get(structKey).isValidState(converter, loc))) {
				Stargate.log(Level.INFO, structKey + "returned negative");
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
	
	public static HashMap<Material, List<GateFormat>> loadGateFormats(String gateFolder) {
		HashMap<Material, List<GateFormat>> controlToGateMap = new HashMap<>();
		File dir = new File(gateFolder);
		File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

		for (File file : files) {
			Stargate.log(Level.FINER, "Reading gateFormat from " + file.getName());
			GateFormatParser gateParser = new GateFormatParser(file);
			try {
				gateParser.open();
				GateFormat format = gateParser.parse();
				addGateFormat(controlToGateMap, format, gateParser.controlMaterials);
			} catch (FileNotFoundException | GateFormatParser.ParsingError e) {
				Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
			} finally {
				gateParser.close();
			}
		}
		return controlToGateMap;
	}

	private static void addGateFormat(HashMap<Material, List<GateFormat>> register, GateFormat format,
			HashSet<Material> controlMaterials) {
		for (Material mat : controlMaterials) {
			if (!(register.containsKey(mat))) {
				List<GateFormat> gateFormatList = new ArrayList<>();
				register.put(mat, gateFormatList);
			}
			register.get(mat).add(format);
		}
	}

	public static List<GateFormat> getPossibleGatesFromControll(Material controlBlockId) {
		return controlMaterialFormatsMap.get(controlBlockId);
		
	}
	
	public List<BlockVector> getControllBlocks() {
		GateControll controll = (GateControll) portalParts.get(CONTROLLKEY);
		
		return controll.parts;
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
		HashSet<Material> controlMaterials;

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

			return new GateFormat(iris, frame, control, remainingConfig, this.file.getName());
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
				if(symbol == '-') {
					controlMaterials = id;
				}
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
			int lineNr, i = 0;
			for (lineNr = 0; lineNr < lines.size(); lineNr++) {
				char[] charLine = lines.get(lineNr).toCharArray();
				for (i = 0; i < charLine.length; i++) {
					BlockVector selectedLocation = new BlockVector(0, -lineNr, i);
					setDesignPoint(charLine[i], selectedLocation.clone());
				}
			}
		}
		
		/**
		 * Determines how one char point in the design should be added into the gateStructure
		 * @param key
		 * @param selectedLocation
		 * @throws ParsingError
		 */
		private void setDesignPoint(char key, BlockVector selectedLocation) throws ParsingError {
			switch (key) {
			case NOTINGATE:
				break;
			case EXIT:
				iris.addExit(selectedLocation.clone());
				break;
			case ENTRANCE:
				iris.addPart(selectedLocation.clone());
				break;
			case CONTROL:
				frame.addPart(selectedLocation, frameMaterials.get(key));
				BlockVector controlLocation = selectedLocation.clone();
				controlLocation.add(new BlockVector(1, 0, 0));
				control.addPart(controlLocation);
				break;
			default:
				if ((key == '?') || (!frameMaterials.containsKey(key))) {
					throw new ParsingError("Unknown symbol '" + key + "' in gatedesign");
				}
				frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
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

	public Material getPortalClosedMat() {
		//TODO Temporary solution
		for(Material mat : ((GateIris)portalParts.get(IRISKEY)).irisClosed)
			return mat;
		return Material.AIR;
	}
}
