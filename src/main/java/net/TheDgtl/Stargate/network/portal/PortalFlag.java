package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.exception.NoFlagFound;

import java.util.EnumSet;
import java.util.HashMap;

public enum PortalFlag {
    RANDOM('R'), FANCY_INTER_SERVER('I'), ALWAYS_ON('A'), BACKWARDS('B'),
    HIDDEN('H'), PRIVATE('P'), FORCE_SHOW('S'), HIDE_NETWORK('N'),
    FREE('F'), BUNGEE('U'), SILENT('Q'),
    //Internal
    NETWORKED('1'), FIXED('2'), IRON_DOOR('3'), PERSONAL_NETWORK('4');

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