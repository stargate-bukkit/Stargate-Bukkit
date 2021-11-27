package net.TheDgtl.Stargate.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GateFrame extends GateStructure {
    Map<BlockVector, Set<Material>> parts;

    public GateFrame() {
        parts = new HashMap<>();
    }

    public void addPart(BlockVector vec, Set<Material> set) {
        parts.put(vec, set);
    }

    @Override
    public void generateBlocks() {
        // TODO Auto-generated method stub

    }


    @Override
    protected List<BlockVector> getPartsPos() {
        return new ArrayList<>(parts.keySet());
    }


    @Override
    protected boolean isValidBlock(BlockVector vec, Material mat) {
        return parts.get(vec).contains(mat);
    }
}
