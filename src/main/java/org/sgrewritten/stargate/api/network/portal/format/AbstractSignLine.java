package org.sgrewritten.stargate.api.network.portal.format;

import org.sgrewritten.stargate.property.NonLegacyClass;

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
        return this.getClass().getName() + getComponents();
    }
}
