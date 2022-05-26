package net.TheDgtl.Stargate.config;

/**
 * An enum defining the different data types an option can have
 */
public enum OptionDataType {

    /**
     * The data type if the option is a String
     */
    STRING,

    /**
     * The data type if the option is a string color
     */
    COLOR(generateColors()),

    /**
     * The data type if the option is a Boolean
     */
    BOOLEAN,

    /**
     * The data type if the option is a string list
     */
    STRING_LIST,

    /**
     * The data type if the option is an Integer
     */
    INTEGER,

    /**
     * The data type if the option is a double
     */
    DOUBLE,

    /**
     * The data type if the option is a string language
     */
    LANGUAGE(generateLanguages()),

    /**
     * The data type if the option is a string logging level
     */
    LOGGING_LEVEL(generateLoggingLevels()),

    /**
     * The data type if the option is a string remote database driver
     */
    REMOTE_DATABASE_DRIVER(generateDrivers());

    private final String[] values;

    /**
     * Instantiates a new option data type with no default options
     */
    OptionDataType() {
        values = new String[]{};
    }

    /**
     * Instantiates a new option data type with the given values
     *
     * @param values <p>The values available for the new option data type</p>
     */
    OptionDataType(String[] values) {
        this.values = values;
    }

    /**
     * Gets a list of available values for this data type
     *
     * <p>Null will be returned for option types with an undefined value scope.</p>
     *
     * @return <p>A list of available values</p>
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Generates a list of languages for the language data type
     *
     * @return <p>A list of language codes</p>
     */
    private static String[] generateLanguages() {
        return new String[]{"af", "ar", "ca", "cs", "da", "de", "el", "en", "en-CA", "en-GB", "en-PT", "en-UD",
                "en-US", "en-ES", "fi", "fr", "he", "hu", "it", "ja", "ko", "lol", "nb", "nl", "nn", "nn-NO", "pl",
                "pt", "pt-BR", "pt-PT", "ro", "ru", "sr", "sv", "sv-SE", "tr", "uk", "vi", "zh", "zh-CN", "zh-TW"};
    }

    /**
     * Generates a list of logging levels for the logging level data type
     *
     * @return <p>A list of logging levels</p>
     */
    private static String[] generateLoggingLevels() {
        return new String[]{"SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST"};
    }

    /**
     * Generates a list of available database drivers for the remote database driver data type
     *
     * @return <p>A list of available data drivers</p>
     */
    private static String[] generateDrivers() {
        return new String[]{"MySQL", "MariaDB"};
    }

    /**
     * Generates a list of available colors for the color data type
     *
     * @return <p>A list of available colors</p>
     */
    private static String[] generateColors() {
        return new String[]{"AQUA", "BLACK", "BLUE", "BOLD", "DARK_AQUA", "DARK_BLUE", "DARK_GRAY", "DARK_GREEN",
                "DARK_PURPLE", "DARK_RED", "GOLD", "GRAY", "GREEN", "LIGHT_PURPLE", "RED", "WHITE", "YELLOW"};
    }

}
