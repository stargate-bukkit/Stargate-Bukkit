package net.TheDgtl.Stargate;

public interface LanguageAPI {

    /**
     * Gets a formatted error message
     *
     * @param translatableMessage <p>The translatable message to display as an error</p>
     * @return <p>The formatted error message</p>
     */
    public String getErrorMessage(TranslatableMessage translatableMessage);

    /**
     * Gets a formatted message
     *
     * @param translatableMessage <p>The translatable message to display</p>
     * @return <p>The formatted message</p>
     */
    public String getMessage(TranslatableMessage translatableMessage);

    /**
     * Gets a translated string
     *
     * @param translatableMessage <p>The translatable message to translate</p>
     * @return <p>The corresponding translated message</p>
     */
    public String getString(TranslatableMessage translatableMessage);

    /**
     * Sets the currently used language
     *
     * <p>Sets the language and loads everything from the language file</p>
     *
     * @param language <p>The language to change to</p>
     */
    public void setLanguage(String language);
}
