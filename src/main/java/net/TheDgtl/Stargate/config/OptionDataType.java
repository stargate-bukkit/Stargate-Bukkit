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

    
    private final Object[] options;
    
    private OptionDataType() {
        options = new Object[0];
    }
    
    private OptionDataType(Object[] options) {
        this.options = options;
    }
    
    public Object[] getOptions() {
        return options;
    }

    private static String[] generateLanguages() {
        return new String[] { "af", "ar", "ca", "cs", "da", "de", "el", "en", "en-CA", "en-GB", "en-PT", "en-UD",
                "en-US", "en-ES", "fi", "fr", "he", "hu", "it", "ja", "ko", "lol", "nb", "nl", "nn", "nn-NO", "pl",
                "pt", "pt-BR", "pt-PT", "ro", "ru", "sr", "sv", "sv-SE", "tr", "uk", "vi", "zh", "zh-CN", "zh-TW" };
    }

    private static String[] generateLoggingLevels() {
        return new String[] { "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST" };
    }

    private static String[] generateDrivers() {
        return new String[] { "MySQL", "MariaDB" };
    }

    private static String[] generateColors() {
        return new String[] { "AQUA", "BLACK", "BLUE", "BOLD", "DARK_AQUA", "DARK_BLUE", "DARK_GRAY", "DARK_GREEN",
                "DARK_PURPLE", "DARK_RED", "GOLD", "GRAY", "GREEN", "LIGHT_PURPLE", "RED", "WHITE", "YELLOW" };
    }
}
