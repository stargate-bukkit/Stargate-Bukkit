package net.TheDgtl.Stargate.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GateFrame extends GateStructure {
    HashMap<BlockVector, HashSet<Material>> parts;

    public GateFrame() {
        parts = new HashMap<>();
    }

    public void addPart(BlockVector vec, HashSet<Material> hashSet) {
        parts.put(vec, hashSet);
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
