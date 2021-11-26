package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;

import java.util.logging.Level;

public class PortalColorParser {

    private Sign sign;
    private boolean isLightSign;
    private ChatColor defaultLightColor;
    private ChatColor defaultDarkColor;
    static private ChatColor GRAY_SELECTOR_COLOR = ChatColor.of("#808080");
    static private ChatColor ERROR_COLOR = ChatColor.RED;

    @SuppressWarnings("deprecation")
    public PortalColorParser(Sign sign) {
        defaultLightColor = ChatColor.valueOf(Setting.getString(Setting.DEFAULT_LIGHT_SIGN_COLOR));
        defaultDarkColor = ChatColor.valueOf(Setting.getString(Setting.DEFAULT_DARK_SIGN_COLOR));
        this.sign = sign;
        this.isLightSign = isLightSign();


    }

    /**
     * Compiles how this portal will look like on a sign, includes the
     *
     * @param surround
     * @param portal
     * @return
     */
    public String parseName(NameSurround surround, IPortal portal) {
        String name = portal.getName();

        ChatColor nameColor;
        ChatColor selectorColor;

        switch (Setting.getInteger(Setting.NAME_STYLE)) {
            case 1:
            case 2:
            default:
                nameColor = getNameColor(portal, isLightSign);
                break;
            case 3:
                nameColor = getDefaultColor(isLightSign);
                break;
        }

        switch (Setting.getInteger(Setting.NAME_STYLE)) {
            case 1:
                selectorColor = getDefaultColor(isLightSign);
                break;
            case 2:
                selectorColor = getNameColor(portal, isLightSign);
                break;
            case 3:
                selectorColor = GRAY_SELECTOR_COLOR;
                break;
            case 4:
                selectorColor = getDefaultColor(!isLightSign);
                break;
            default:
                selectorColor = GRAY_SELECTOR_COLOR;
        }

        String coloredName = nameColor + name + selectorColor;
        return selectorColor + surround.getSurround(coloredName);

    }

    public String parseLine(String line) {
        return getColor(isLightSign) + line;
    }

    public String parseError(String error, NameSurround surround) {
        return getColor(isLightSign) + surround.getSurround(ERROR_COLOR + error + getColor(isLightSign));
    }

    protected boolean isLightSign() {

        switch (sign.getType()) {
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

    @SuppressWarnings("deprecation")
    private ChatColor getColor(boolean isLightSign) {
        if (sign.getColor() != DyeColor.BLACK) {
            return ChatColor.valueOf(sign.getColor().toString());
        }
        return getDefaultColor(isLightSign);
    }

    private ChatColor getDefaultColor(boolean isLightSign) {
        return isLightSign ? ChatColor.BLACK : ChatColor.WHITE;
    }

    private ChatColor getNameColor(IPortal portal, boolean isLightSign) {
        Stargate.log(Level.FINEST, " Gate " + portal.getName() + " has flags: " + portal.getAllFlagsString());
        ChatColor[] colors = new ChatColor[]{getDefaultColor(true), getDefaultColor(false)};

        if (portal.hasFlag(PortalFlag.BACKWARDS)) {
            colors = new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#b3baff")};
        }
        if (portal.hasFlag(PortalFlag.FORCE_SHOW)) {
            colors = new ChatColor[]{ChatColor.of("#002422"), ChatColor.of("#b3fffc")};
        }
        if (portal.hasFlag(PortalFlag.HIDDEN)) {
            colors = new ChatColor[]{ChatColor.of("#292800"), ChatColor.of("#fffcb3")};
        }
        if (portal.hasFlag(PortalFlag.FREE)) {
            colors = new ChatColor[]{ChatColor.of("#002402"), ChatColor.of("#b3ffb8")};
        }
        if (portal.hasFlag(PortalFlag.PRIVATE)) {
            colors = new ChatColor[]{ChatColor.of("#210000"), ChatColor.of("#ffb3b3")};
        }
        if (portal instanceof VirtualPortal) {
            colors = new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#FFE0FE")};
        }
        return (isLightSign ? colors[0] : colors[1]);
    }

}
