package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;

public class PortalBlockGenerator {
    
    /**
     * Generates an obsidian portal
     * @param bottomLeft <p> The bottom left of the portal </p>
     * @return <p> The signblock of the portal </p>
     */
    public static Block generatePortal(Location bottomLeft) {
        int[][] portal = {
                {1,1,1,1},
                {1,0,0,1},
                {1,0,0,1},
                {1,0,0,1},
                {1,1,1,1}};
        for(int y = 0; y < portal.length; y++) {
            for(int x = 0; x < portal[y].length; x++) {
                Location location = bottomLeft.clone().add(x,y,0);
                Material material;
                switch (portal[y][x]) {
                case 1:
                    material = Material.OBSIDIAN;
                    break;
                case 0:
                    material = Material.AIR;
                    break;
                default:
                    continue;
                }
                location.getBlock().setType(material);
            }
        }
        Block signBlock = bottomLeft.clone().add(0, 2, 1).getBlock();
        signBlock.setType(Material.ACACIA_WALL_SIGN);
        ((WallSign)signBlock.getBlockData()).setFacing(BlockFace.SOUTH);
        return signBlock;
    }
}
