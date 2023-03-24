package net.knarcraft.stargate.container;

import net.knarcraft.knarlib.util.ColorHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;

/**
 * A class that keeps track of the sign colors for a given sign
 */
public class SignData {

    private final Sign sign;
    private final ChatColor mainSignColor;
    private final ChatColor highlightSignColor;
    private final DyeColor dyedColor;

    /**
     * Instantiates a new sign colors object
     *
     * @param sign               <p>The sign the colors belong to</p>
     * @param mainSignColor      <p>The main color to use for the sign</p>
     * @param highlightSignColor <p>The highlighting color to use for the sign</p>
     */
    public SignData(Sign sign, ChatColor mainSignColor, ChatColor highlightSignColor) {
        this.sign = sign;
        this.mainSignColor = mainSignColor;
        this.highlightSignColor = highlightSignColor;
        this.dyedColor = sign.getColor();
    }

    /**
     * Gets the sign of this sign colors object
     *
     * @return <p>The sign of this sign colors object</p>
     */
    public Sign getSign() {
        return sign;
    }

    /**
     * Gets the main color of the sign
     *
     * @return <p>The main color of the sign</p>
     */
    public ChatColor getMainSignColor() {
        if (dyedColor != DyeColor.BLACK) {
            return ColorHelper.fromColor(dyedColor.getColor());
        } else {
            return mainSignColor;
        }
    }

    /**
     * Gets the highlighting color of the sign
     *
     * @return <p>The highlighting color of the sign</p>
     */
    public ChatColor getHighlightSignColor() {
        if (dyedColor != DyeColor.BLACK) {
            return ColorHelper.fromColor(ColorHelper.invert(dyedColor.getColor()));
        } else {
            return highlightSignColor;
        }
    }

}
