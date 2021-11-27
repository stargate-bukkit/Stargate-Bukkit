package net.TheDgtl.Stargate.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GateIris extends GateStructure {

    public final Set<Material> irisOpen;
    public final Set<Material> irisClosed;
    BlockVector exit;
    protected List<BlockVector> blocks;


    public GateIris(Set<Material> irisOpen, Set<Material> irisClosed) {
        this.irisOpen = irisOpen;
        this.irisClosed = irisClosed;
        blocks = new ArrayList<>();
    }

    public void addPart(BlockVector blockVector) {
        blocks.add(blockVector);
    }

    public void addExit(BlockVector exitpoint) {
        this.exit = exitpoint.clone();
        addPart(exitpoint);
    }

    public void open() {
        //TODO
    }

    public void close() {
        //TODO
    }

    @Override
    public void generateBlocks() {
        // TODO Auto-generated method stub

    }

    @Override
    protected List<BlockVector> getPartsPos() {
        return blocks;
    }

    @Override
    protected boolean isValidBlock(BlockVector vec, Material mat) {
        return irisClosed.contains(mat) || irisOpen.contains(mat);
    }

    public Material getMat(boolean isOpen) {
        //TODO a bit lazy
        return (isOpen ? irisOpen : irisClosed).iterator().next();
    }

    public BlockVector getExit() {
        return exit;
    }
}
