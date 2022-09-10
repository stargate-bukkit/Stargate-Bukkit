package net.TheDgtl.Stargate.network.portal.formatting;

import org.bukkit.DyeColor;

import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;

/**
 * A formatter for formatting a line on a sign
 */
public interface LineFormatter {

    /**
     * Formats the name of a portal
     *
     * @param portal            <p>The portal whose name is to be formatted</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted name</p>
     */
    String formatPortalName(Portal portal, HighlightingStyle highlightingStyle);

    /**
     * Formats the name of a portal
     *
     * @param network            <p>The network whose name is to be formatted</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted name</p>
     */
    String formatNetworkName(Network network, HighlightingStyle highlightingStyle);
    
    /**
     * Formats the name of a portal
     *
     * @param aString           <p>Any type of string</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted string</p>
     */
    String formatStringWithHiglighting(String aString, HighlightingStyle highlightingStyle);
    
    /**
     * Formats a line using the default behaviour
     *
     * @param line <p>The line to format</p>
     * @return <p>The formatted line</p>
     */
    String formatLine(String line);

    /**
     * Formats an error line to display on a sign
     *
     * @param error             <p>The error message to format</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted error</p>
     */
    String formatErrorLine(String error, HighlightingStyle highlightingStyle);
    
    /**
     * What should happen when the sign related to this LineFormatter gets dyed by a user?
     * @param signColor <p> </p>
     */
    void onSignDyeing(DyeColor signColor);

}