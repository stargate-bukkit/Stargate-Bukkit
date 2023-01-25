package org.sgrewritten.stargate.util;

import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class FakeLanguageManager implements LanguageManager {

    @Override
    public String getErrorMessage(TranslatableMessage translatableMessage) {
        return "[ERROR] " + translatableMessage;
    }

    @Override
    public String getWarningMessage(TranslatableMessage translatableMessage) {
        return "[WARNING]" + translatableMessage;
    }

    @Override
    public String getMessage(TranslatableMessage translatableMessage) {
        return "[INFO]" + translatableMessage;
    }

    @Override
    public String getString(TranslatableMessage translatableMessage) {
        return translatableMessage.toString();
    }

    @Override
    public void setLanguage(String language) {
    }

}
