package net.TheDgtl.Stargate;

public class FakeLanguageManager implements LanguageAPI {

    @Override
    public String getErrorMessage(TranslatableMessage translatableMessage) {
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
