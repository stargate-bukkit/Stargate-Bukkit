package org.sgrewritten.stargate.gate.structure;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;
import org.sgrewritten.stargate.manager.BlockDropManager;
import org.sgrewritten.stargate.util.ButtonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one of the control blocks in a gate structure
 */
public class GateControlBlock extends GateStructure {

    final List<BlockVector> parts;
    private final BoundingBox boundingBox;
    private static final Material SIGN_MATERIAL = Material.OAK_WALL_SIGN;

    /**
     * Instantiates a new gate control block container
     */
    public GateControlBlock() {
        parts = new ArrayList<>();
        this.boundingBox = new BoundingBox();
    }

    /**
     * Adds a vector to the list of control blocks
     *
     * @param blockVector <p>The block vector to add</p>
     */
    public void addPart(BlockVector blockVector) {
        parts.add(blockVector);
        boundingBox.union(blockVector);
    }

    @Override
    public List<BlockVector> getStructureTypePositions() {
        return parts;
    }

    @Override
    protected boolean isValidBlock(BlockVector blockVector, Material material) {
        material = material.isLegacy() ? XMaterial.matchXMaterial(material).parseMaterial() : material;
        if (Tag.WALL_SIGNS.isTagged(material) || ButtonHelper.isButton(material)) {
            return true;
        }
        return material.isAir() || material == Material.WATER || material == Material.LIGHT;
    }

    public void generateStructure(VectorOperation converter, Location topLeft) {
        if (parts.stream().map(position -> topLeft.clone().add(converter.performToRealSpaceOperation(position)).getBlock().getType())
                .anyMatch(Tag.WALL_SIGNS::isTagged)) {
            return;
        }
        Block signLocation = topLeft.clone().add(converter.performToRealSpaceOperation(parts.get(0))).getBlock();
        BlockState state = signLocation.getState();
        state.setType(Material.OAK_WALL_SIGN);
        WallSign signData = (WallSign) state.getBlockData();
        signData.setFacing(converter.getFacing());
        state.setBlockData(signData);
        state.update(true);
        BlockDropManager.disableBlockDrops(signLocation);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }
}
