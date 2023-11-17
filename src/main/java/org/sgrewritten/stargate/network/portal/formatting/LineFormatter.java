package org.sgrewritten.stargate.network.portal.formatting;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.format.StargateComponent;

import java.util.List;

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
    List<StargateComponent> formatPortalName(Portal portal, HighlightingStyle highlightingStyle);

    /**
     * Formats the name of a portal
     *
     * @param network           <p>The network whose name is to be formatted</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted name</p>
     */
    List<StargateComponent> formatNetworkName(Network network, HighlightingStyle highlightingStyle);

    /**
     * Formats the name of a portal
     *
     * @param aString           <p>Any type of string</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted string</p>
     */
    List<StargateComponent> formatStringWithHighlighting(String aString, HighlightingStyle highlightingStyle);

    /**
     * Formats a line using the default behaviour
     *
     * @param line <p>The line to format</p>
     * @return <p>The formatted line</p>
     */
    List<StargateComponent> formatLine(String line);

    /**
     * Formats an error line to display on a sign
     *
     * @param error             <p>The error message to format</p>
     * @param highlightingStyle <p>The highlighting style to use when formatting</p>
     * @return <p>The formatted error</p>
     */
    List<StargateComponent> formatErrorLine(String error, HighlightingStyle highlightingStyle);

}