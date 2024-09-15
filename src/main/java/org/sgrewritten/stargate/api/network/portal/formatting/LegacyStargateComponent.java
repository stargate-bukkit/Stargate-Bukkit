package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.container.Holder;

public final class LegacyStargateComponent implements StargateComponent {

    private final String text;

    public LegacyStargateComponent(@Nullable String text) {
        this.text = text;
    }

    public @Nullable String getText() {
        return text;
    }

    public static Holder<StargateComponent> of(@Nullable String text) {
        return new Holder<>(new LegacyStargateComponent(text));
    }

    @Override
    public void setSignLine(int index, @NotNull Sign sign) {
        if (text != null) {
            sign.setLine(index, text);
        }
    }

    @Override
    public void sendMessage(Entity receiver) {
        if (text != null && !text.isEmpty()) {
            receiver.sendMessage(text);
        }
    }

    @Override
    public StargateComponent append(StargateComponent value) {
        if (value instanceof LegacyStargateComponent legacyStargateComponent) {
            return new LegacyStargateComponent(this.getText() + legacyStargateComponent.getText());
        }
        if (value instanceof EmptyStargateComponent) {
            return new LegacyStargateComponent(this.getText());
        }
        throw new IllegalArgumentException("Can not combine with AdventureComponent");
    }

    @Override
    public String plainText() {
        if (text == null) {
            return "";
        }
        return ChatColor.stripColor(text);
    }
}
