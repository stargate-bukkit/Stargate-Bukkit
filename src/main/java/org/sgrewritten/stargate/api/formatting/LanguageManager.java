package org.sgrewritten.stargate.api.formatting;

import org.sgrewritten.stargate.formatting.TranslatableMessage;

public interface LanguageManager {

    /**
     * Gets a formatted error message
     *
     * @param translatableMessage <p>The translatable message to display as an error</p>
     * @return <p>The formatted error message</p>
     */
    String getErrorMessage(TranslatableMessage translatableMessage);

    /**
     * Gets a formatted warning message
     *
     * @param translatableMessage <p>The translatable message to display as an error</p>
     * @return <p>The formatted warning message</p>
     */
    String getWarningMessage(TranslatableMessage translatableMessage);

    /**
     * Gets a formatted message
     *
     * @param translatableMessage <p>The translatable message to display</p>
     * @return <p>The formatted message</p>
     */
    String getMessage(TranslatableMessage translatableMessage);

    /**
     * Gets a translated string
     *
     * @param translatableMessage <p>The translatable message to translate</p>
     * @return <p>The corresponding translated message</p>
     */
    String getString(TranslatableMessage translatableMessage);

    /**
     * Sets the currently used language
     *
     * <p>Sets the language and loads everything from the language file</p>
     *
     * @param language <p>The language to change to</p>
     */
    void setLanguage(String language);

}
