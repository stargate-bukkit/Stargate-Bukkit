package net.TheDgtl.Stargate.util;

public class NameHelper {
    
    /**
     * 
     * @param name <p> The name to check </p>
     * @return     <p> The allowed name </P>
     */
    public static String getAllowedName(String name) {
        name = name.replaceAll("\s\s+", " ");
        return name.trim();
    }
}
