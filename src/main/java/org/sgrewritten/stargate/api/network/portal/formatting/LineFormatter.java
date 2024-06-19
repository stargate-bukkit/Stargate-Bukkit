package org.sgrewritten.stargate.api.network.portal.formatting;

import org.sgrewritten.stargate.api.network.portal.PortalPositionAttachment;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;

/**
 * A formatter for formatting a line on a sign
 */
public interface LineFormatter extends PortalPositionAttachment {

    /**
     * @param lineData <p>The line data to format</p>
     * @return <p>Formatted sign lines</p>
     */
    default SignLine[] formatLineData(LineData[] lineData){
        SignLine[] output = new SignLine[lineData.length];
        for (int i = 0; i < lineData.length; i++) {
            output[i] = convertToSignLine(lineData[i]);
        }
        return output;
    }

    SignLine convertToSignLine(LineData lineData);

    @Override
    default Type getType() {
        return Type.LINE_FORMATTER;
    }
}