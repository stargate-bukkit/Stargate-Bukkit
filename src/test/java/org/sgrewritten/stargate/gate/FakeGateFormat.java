package org.sgrewritten.stargate.gate;

import org.bukkit.Material;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;

public class FakeGateFormat implements GateFormatAPI {
    @Override
    public String getFileName() {
        return "invalid";
    }

    @Override
    public Material getIrisMaterial(boolean getOpenMaterial) {
        return getOpenMaterial ? Material.WATER : Material.AIR;
    }
}
