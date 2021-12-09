package net.TheDgtl.Stargate.network.portal;

public interface ILineCompiler {
    public String compilePortalName(HighlightingStyle surround, IPortal portal);

    String compileLine(String line);

    String compileErrorLine(String error, HighlightingStyle surround);
}
