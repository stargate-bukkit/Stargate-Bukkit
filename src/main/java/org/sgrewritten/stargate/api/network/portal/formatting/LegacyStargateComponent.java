package org.sgrewritten.stargate.api.network.portal.formatting;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.container.Holder;

public final class LegacyStargateComponent implements StargateComponent {

    private final String text;

    public LegacyStargateComponent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static Holder<StargateComponent> of(String text) {
        return new Holder<>(new LegacyStargateComponent(text));
    }

    @Override
    public void setSignLine(int index, Sign sign) {
        sign.setLine(index, text);
    }

    @Override
    public void sendMessage(Entity receiver) {
        receiver.sendMessage(text);
    }

    @Override
    public StargateComponent append(StargateComponent value) {
        if (value instanceof LegacyStargateComponent legacyStargateComponent) {
            return new LegacyStargateComponent(this.getText() + legacyStargateComponent.getText());
        }
        if (value instanceof EmptyStargateComponent){
            return new LegacyStargateComponent(this.getText());
        }
        throw new IllegalArgumentException("Can not combine with AdventureComponent");
    }

    @Override
    public String plainText() {
        return ChatColor.stripColor(text);
    }
}
