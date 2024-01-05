package org.sgrewritten.stargate.api.network.portal.format;

import java.util.List;

public interface SignLine {
    List<StargateComponent> getComponents();

    SignLineType getType();
}
