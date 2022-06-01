package net.TheDgtl.Stargate.network.portal.formatting;

import org.bukkit.Material;

public abstract class AbstractLineColorFormatter implements LineFormatter{
    
    protected final boolean isLightSign;
    public AbstractLineColorFormatter(Material signMaterial) {
        this.isLightSign = isLightSign(signMaterial);
    }
    /**
     * Checks whether the given sign material represents a light sign or a dark sign
     *
     * @param signMaterial <p>The sign material to check</p>
     * @return <p>True if the material represents a light sign</p>
     */
    static private boolean isLightSign(Material signMaterial) {
        switch (signMaterial) {
            // Dark signs
            case DARK_OAK_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
                return false;
            default:
                return true;
        }
    }
}
