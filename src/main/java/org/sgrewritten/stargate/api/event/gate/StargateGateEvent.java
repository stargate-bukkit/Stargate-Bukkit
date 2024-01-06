package org.sgrewritten.stargate.api.event.gate;

import org.bukkit.event.Event;
import org.sgrewritten.stargate.api.gate.GateAPI;

public abstract class StargateGateEvent extends Event {


    private final GateAPI gate;

    StargateGateEvent(GateAPI gate) {
        this.gate = gate;
    }

    /**
     * Get the gate this event relates to
     * @return <p>The gate this event relates to</p>
     */
    public GateAPI getGate() {
        return this.gate;
    }
}
