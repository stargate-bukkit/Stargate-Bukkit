package org.sgrewritten.stargate.api.gate.control;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;

import java.util.UUID;

public interface GateActivationHandler extends ControlMechanism {

    /**
     * Whether the activation control is active. This gets checked whenever {@link Portal#updateState()} gets triggered
     *
     * @return <p>Whether the control is active at the moment of time</p>
     */
    boolean isActive();

    /**
     * Get the player which activated this control
     *
     * @return <p>The uuid player which activated this control</p>
     */
    @Nullable UUID getActivator();
}
