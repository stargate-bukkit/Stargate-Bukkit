package org.sgrewritten.stargate.api.event.gate;

import org.bukkit.event.Event;
import org.sgrewritten.stargate.api.gate.GateAPI;

public abstract class StargateGateEvent extends Event {


    private final GateAPI gate;

    StargateGateEvent(GateAPI gate) {
        this.gate = gate;
    }

    public GateAPI getGate() {
        return this.gate;
    }
}
