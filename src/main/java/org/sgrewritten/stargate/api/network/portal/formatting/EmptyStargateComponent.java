package org.sgrewritten.stargate.api.network.portal.formatting;

import com.drew.lang.annotations.NotNull;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

final class EmptyStargateComponent implements StargateComponent {

    @Override
    public void setSignLine(int index, @NotNull Sign sign) {
        sign.setLine(index, "");
    }

    @Override
    public void sendMessage(Entity receiver) {
        // empty component
    }

    @Override
    public StargateComponent append(StargateComponent value) {
        if (!(value instanceof EmptyStargateComponent)) {
            return value.append(this);
        }
        return new EmptyStargateComponent();
    }

    @Override
    public String plainText() {
        return "";
    }
}