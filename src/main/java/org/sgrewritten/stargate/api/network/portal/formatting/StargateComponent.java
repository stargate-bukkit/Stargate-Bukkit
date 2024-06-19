package org.sgrewritten.stargate.api.network.portal.formatting;

import com.drew.lang.annotations.NotNull;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

/**
 * An interface for text components, meant to support both spigot and adventure text formats.
 */
public sealed interface StargateComponent permits LegacyStargateComponent, AdventureStargateComponent, EmptyStargateComponent {

    /**
     * @return An empty component with no text
     */
    static StargateComponent empty() {
        return new EmptyStargateComponent();
    }

    /**
     * Set a line with the text of this component in the specified sign state
     * @param index <p>The index of the line to change</p>
     * @param sign <p>The sign state to modify</p>
     */
    void setSignLine(int index, @NotNull Sign sign);

    /**
     *
     * @param receiver
     */
    void sendMessage(Entity receiver);

    /**
     * Append another components content after this component
     * @param value <p>The other component to append</p>
     * @return <p>The resulting component after the merge</p>
     */
    StargateComponent append(StargateComponent value);

    /**
     * @return A plain text serialization (no colors and so on)
     */
    String plainText();
}
