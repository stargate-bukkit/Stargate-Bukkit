package org.sgrewritten.stargate.api.network.portal;

public interface PortalPositionAttachment {


    Type getType();

    enum Type {
        LINE_FORMATTER
    }
}
