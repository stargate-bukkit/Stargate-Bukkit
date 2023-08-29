package org.sgrewritten.stargate.api.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.List;

public interface GateFormatAPI {
    /**
     * @return <p> The name of the file this format was loaded from </p>
     */
    String getFileName();

    List<BlockVector> getControlBlocks();

    /**
     * Gets the material used for this gate format's iris when in the given state
     *
     * @param getOpenMaterial <p>Whether to get the open-material or the closed-material</p>
     * @return <p>The material used for this gate format's iris</p>
     */
    Material getIrisMaterial(boolean getOpenMaterial);
}
