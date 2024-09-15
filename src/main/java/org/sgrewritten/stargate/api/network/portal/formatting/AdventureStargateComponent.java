package org.sgrewritten.stargate.api.network.portal.formatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.container.Holder;

import java.util.Objects;

public final class AdventureStargateComponent implements StargateComponent {

    private final Component text;

    public AdventureStargateComponent(@NotNull Component text) {
        this.text = Objects.requireNonNull(text);
    }

    public @NotNull Component getText() {
        return text;
    }

    public static Holder<StargateComponent> of(@NotNull Component text) {
        return new Holder<>(new AdventureStargateComponent(text));
    }

    @Override
    public void setSignLine(int index,@NotNull  Sign sign) {
        sign.line(index, text);
    }

    @Override
    public void sendMessage(Entity receiver) {
        receiver.sendMessage(text);
    }

    @Override
    public StargateComponent append(StargateComponent value) {
        if (value instanceof EmptyStargateComponent) {
            return new AdventureStargateComponent(this.getText());
        }
        if (value instanceof LegacyStargateComponent legacyStargateComponent) {
            Component legacyConvertedText = LegacyComponentSerializer.legacySection().deserialize(legacyStargateComponent.getText());
            return new AdventureStargateComponent(getText().append(legacyConvertedText));
        }
        if (value instanceof AdventureStargateComponent adventureStargateComponent) {
            return new AdventureStargateComponent(getText().append(adventureStargateComponent.getText()));
        }
        throw new IllegalStateException("A state that should not have been reached, has been reached");
    }

    @Override
    public String plainText() {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }
}
