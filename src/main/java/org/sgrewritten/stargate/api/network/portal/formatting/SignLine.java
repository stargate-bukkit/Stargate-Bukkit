package org.sgrewritten.stargate.api.network.portal.formatting;

import java.util.List;

public interface SignLine {

    /**
     * @return <p>The components this line contains</p>
     */
    List<StargateComponent> getComponents();

    /**
     * @return <p>The type of line that is being displayed</p>
     */
    SignLineType getType();
}
