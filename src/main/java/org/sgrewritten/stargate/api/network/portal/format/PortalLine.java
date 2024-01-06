package org.sgrewritten.stargate.api.network.portal.format;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PortalLine extends AbstractSignLine {
    private static final Set<SignLineType> allowedSignLineTypes = Set.of(SignLineType.PORTAL, SignLineType.DESTINATION_PORTAL, SignLineType.THIS_PORTAL);
    private final Portal portal;
    private final SignLineType type;

    public PortalLine(List<StargateComponent> components, @Nullable Portal portal, SignLineType type) {
        super(components);
        Preconditions.checkArgument(allowedSignLineTypes.contains(type), "Disallowed sign line type '" + type + "'");
        this.type = type;
        this.portal = portal;
    }

    public PortalLine(HighlightingStyle style, @Nullable Portal portal, ChatColor textColor, ChatColor pointerColor, SignLineType type) {
        this(new ArrayList<>(), portal, type);
        List<StargateComponent> components = getComponents();
        components.add(new StargateComponent(pointerColor + style.getPrefix()));
        components.add(new StargateComponent(textColor + (portal == null ? "null" : portal.getName())));
        components.add(new StargateComponent(pointerColor + style.getSuffix()));
    }

    @Override
    public SignLineType getType() {
        return this.type;
    }

    /**
     * @return <p>The portal this line relates to</p>
     */
    public Portal getPortal() {
        return this.portal;
    }
}
