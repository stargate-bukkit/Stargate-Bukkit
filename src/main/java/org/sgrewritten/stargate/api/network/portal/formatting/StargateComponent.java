package org.sgrewritten.stargate.api.network.portal.formatting;

import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

/**
 * An interface for text components, meant to allow spigot support.
 */
public sealed interface StargateComponent permits LegacyStargateComponent, AdventureStargateComponent, EmptyStargateComponent {

    static StargateComponent empty() {
        return new EmptyStargateComponent();
    }

    void setSignLine(int index, Sign sign);

    void sendMessage(Entity receiver);

    StargateComponent append(StargateComponent value);

    String plainText();
}
