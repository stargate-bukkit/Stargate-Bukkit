package org.sgrewritten.stargate.exception;

import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;

import java.io.Serial;
import java.util.Objects;
import java.util.Set;

public class UnimplementedFlagException extends TranslatableException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -8052738578093828433L;
    private final @NotNull PortalFlag flag;

    public UnimplementedFlagException(String msg, @NotNull PortalFlag flag) {
        super(msg);
        this.flag = Objects.requireNonNull(flag);
    }

    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return TranslatableMessage.UNIMPLEMENTED_FLAG;
    }

    @Override
    public String getLocalisedMessage(LanguageManager manager) {
        return TranslatableMessageFormatter.formatFlags(manager.getErrorMessage(getTranslatableMessage()), Set.of(flag));
    }
}
