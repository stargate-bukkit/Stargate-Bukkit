package net.TheDgtl.Stargate.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class GateControll extends GateStructure {
    List<BlockVector> parts;

    public GateControll() {
        parts = new ArrayList<>();
    }

    public void addPart(BlockVector vec) {
        parts.add(vec);
    }


    @Override
    public void generateBlocks() {
        // TODO Auto-generated method stub

    }


    @Override
    protected List<BlockVector> getPartsPos() {
        return parts;
    }


    @Override
    protected boolean isValidBlock(BlockVector vec, Material mat) {
        //TODO maybe add some fancy detection here
        return true;
    }
}
