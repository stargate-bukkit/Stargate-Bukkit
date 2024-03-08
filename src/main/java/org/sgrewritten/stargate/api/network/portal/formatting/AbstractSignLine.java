package org.sgrewritten.stargate.api.network.portal.formatting;

import java.util.List;

public abstract class AbstractSignLine implements SignLine {

    private final List<StargateComponent> components;

    protected AbstractSignLine(List<StargateComponent> components) {
        this.components = components;
    }

    @Override
    public List<StargateComponent> getComponents() {
        return components;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + getComponents();
    }
}
