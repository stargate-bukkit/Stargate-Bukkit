package net.TheDgtl.Stargate.network.portal.formatting;

import org.bukkit.Material;

/**
 * An abstract class containing useful methods for all line color formatters
 */
public abstract class AbstractLineColorFormatter implements LineFormatter {

    protected final boolean isLightSign;

    /**
     * Instantiates a new abstract line color formatter
     *
     * @param signMaterial <p>The material of the sign to format for</p>
     */
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
        if (signMaterial != null && Material.getMaterial("MANGROVE_WALL_SIGN") == signMaterial) {
            return false;
        }

        //Prevent a potential NullPointerException
        if (signMaterial == null) {
            return false;
        }

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
