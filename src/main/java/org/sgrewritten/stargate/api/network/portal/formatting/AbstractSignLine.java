package org.sgrewritten.stargate.api.network.portal.formatting;

import org.sgrewritten.stargate.api.container.Holder;
import java.util.List;

public abstract class AbstractSignLine implements SignLine {

    private final List<Holder<StargateComponent>> components;

    protected AbstractSignLine(List<Holder<StargateComponent>> components) {
        this.components = components;
    }

    @Override
    public List<Holder<StargateComponent>> getComponents() {
        return components;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + getComponents();
    }
}
