package org.sgrewritten.stargate.api.network.portal;

public interface PortalPositionAttachment {

    /**
     * @return <p>The type of this attachment</p>
     */
    Type getType();

    enum Type {
        /**
         * Formats text
         */
        LINE_FORMATTER
    }
}
