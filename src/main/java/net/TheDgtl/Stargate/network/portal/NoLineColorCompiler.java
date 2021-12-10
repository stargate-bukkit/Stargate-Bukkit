package net.TheDgtl.Stargate.network.portal;

/**
 * Used for backwards compatibility when ChatColor was not a thing
 *
 * @author Thorin
 */
public class NoLineColorCompiler implements ILineCompiler {

    @Override
    public String compilePortalName(HighlightingStyle surround, IPortal portal) {
        return surround.getHighlightedName(portal.getName());
    }

    @Override
    public String compileLine(String line) {
        return line;
    }

    @Override
    public String compileErrorLine(String error, HighlightingStyle surround) {
        return surround.getHighlightedName(error);
    }

}
