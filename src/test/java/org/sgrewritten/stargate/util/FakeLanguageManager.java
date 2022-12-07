package org.sgrewritten.stargate.util;

import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;

public class FakeLanguageManager implements LanguageManager{

    @Override
    public String getErrorMessage(TranslatableMessage translatableMessage) {
        return "";
    }

    @Override
    public String getWarningMessage(TranslatableMessage translatableMessage) {
        return "";
    }

    @Override
    public String getMessage(TranslatableMessage translatableMessage) {
        return "";
    }

    @Override
    public String getString(TranslatableMessage translatableMessage) {
        return "";
    }

    @Override
    public void setLanguage(String language) {
    }

}
