package net.TheDgtl.Stargate.portal;

import java.util.EnumSet;
import java.util.HashMap;

import net.TheDgtl.Stargate.exception.NoFlagFound;

public enum PortalFlag {
	RANDOM('R'), BUNGEE('U'), ALWAYS_ON('A'), BACKWARDS('B'),
	HIDDEN('H'), PRIVATE('P'), FORCE_SHOW('S'), NO_NETWORK('N'), // ??
	FREE('F');

	public final char label;
	static private final HashMap<Character, PortalFlag> map = new HashMap<>();
	static {
		for (PortalFlag value : values()) {
			map.put(value.label, value);
		}
	}
	
	private PortalFlag(char label) {
		this.label = label;
	}
	
	/**
	 * Go through every character in line, and
	 * 
	 * @param line
	 */
	public static EnumSet<PortalFlag> parseFlags(String line) {
		EnumSet<PortalFlag> foundFlags = EnumSet.noneOf(PortalFlag.class);
		char[] charArray = line.toUpperCase().toCharArray();
		for (char character : charArray) {
			try {
				foundFlags.add(PortalFlag.valueOf(character));
			} catch (NoFlagFound e) {
			}
		}
		return foundFlags;
	}

	public static PortalFlag valueOf(char label) throws NoFlagFound {
		PortalFlag flag = map.get(label);
		if (flag != null)
			return flag;
		throw new NoFlagFound();
	}
}