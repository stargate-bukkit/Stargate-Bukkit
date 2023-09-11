package org.sgrewritten.stargate.api.network.portal.format;

import java.util.List;

public abstract class AbstractSignLine implements SignLine{

    private final List<StargateComponent> components;

    public AbstractSignLine(List<StargateComponent> components){
        this.components = components;
    }

    @Override
    public List<StargateComponent> getComponents() {
        return components;
    }
}
