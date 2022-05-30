package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;

public class FakeLanguageManager implements LanguageManager {

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
