package net.knarcraft.stargate.utility;

import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

/**
 * A helper class for dealing with signs
 */
public final class SignHelper {

    private static final boolean HAS_SIGN_SIDES = hasSignSides();

    private SignHelper() {

    }

    /**
     * Gets the dye color of the given sign
     *
     * @param sign <p>The sign to check</p>
     * @return <p>The dye currently applied to the sign</p>
     */
    public static DyeColor getDye(Sign sign) {
        if (HAS_SIGN_SIDES) {
            return sign.getSide(Side.FRONT).getColor();
        } else {
            // Note: This is depreciated, but is currently necessary for pre-1.19.4 support
            //noinspection deprecation
            return sign.getColor();
        }
    }

    /**
     * Sets the text of a line on a sign
     *
     * @param sign <p>The sign to set text for</p>
     * @param line <p>The line to set</p>
     * @param text <p>The text to set</p>
     */
    public static void setSignLine(Sign sign, int line, String text) {
        if (HAS_SIGN_SIDES) {
            sign.getSide(Side.FRONT).setLine(line, text);
        } else {
            // Note: This is depreciated, but is currently necessary for pre-1.19.4 support
            //noinspection deprecation
            sign.setLine(line, text);
        }
    }

    /**
     * Checks whether the running version differentiates between the front or back of a sign
     *
     * @return <p>True if the server supports sign side differentiation</p>
     */
    private static boolean hasSignSides() {
        try {
            Class.forName("Side");
            Class<?> aClass = Class.forName("org.bukkit.block.Sign");
            aClass.getMethod("getSide", Side.class);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return false;
        }
    }

}
