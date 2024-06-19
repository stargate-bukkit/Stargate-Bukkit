package org.sgrewritten.stargate.exception.name;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;

import java.io.Serial;
import java.util.Optional;

public class NameConflictException extends TranslatableException {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -183581478633277966L;
    private final boolean isNetwork;
    private @Nullable NetworkType networkType = null;

    /**
     * @param message <p>The message to display on the stacktrace for this exception</p>
     */
    public NameConflictException(String message) {
        super(message);
        isNetwork = false;
    }

    /**
     * @param message     <p>The message to display on the stacktrace for this exception</p>
     * @param networkType <p>The network type that relates to this exception</p>
     */
    public NameConflictException(String message, NetworkType networkType) {
        super(message);
        this.isNetwork = true;
        this.networkType = networkType;
    }


    @Override
    protected TranslatableMessage getTranslatableMessage() {
        return isNetwork ? TranslatableMessage.NET_CONFLICT : TranslatableMessage.GATE_ALREADY_EXIST;
    }

    @Override
    public String getLocalisedMessage(LanguageManager languageManager) {
        return Optional.ofNullable(networkType).map(networkType -> {
            String message = languageManager.getErrorMessage(TranslatableMessage.NET_CONFLICT);
            return TranslatableMessageFormatter.formatNetworkType(message, networkType, languageManager);
        }).orElse(super.getLocalisedMessage(languageManager));
    }

}
