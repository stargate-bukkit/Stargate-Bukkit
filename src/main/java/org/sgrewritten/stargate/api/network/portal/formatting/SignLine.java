package org.sgrewritten.stargate.api.network.portal.formatting;

import org.sgrewritten.stargate.api.container.Holder;
import java.util.List;

public interface SignLine {

    /**
     * @return <p>The components this line contains</p>
     */
    List<Holder<StargateComponent>> getComponents();

    /**
     * @return <p>The type of line that is being displayed</p>
     */
    SignLineType getType();
}
