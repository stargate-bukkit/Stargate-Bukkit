package org.sgrewritten.stargate.formatting;

/**
 * A representation of all potentially available languages
 * Used to resolve potential ambiguity.
 */
public enum Language {

    /**
     * The default dialect of Afrikaans is from South Africa.
     */
    AF_ZA("af-ZA", "af"),
    /**
     * The default dialect of Arabic is from Saudi Arabia.
     */
    AR_SA("ar-SA", "ar"),
    /**
     * The default dialect of Catalan is from Spain.
     */
    CA_ES("ca-ES", "ca"),
    /**
     * The default dialect of Czech is from the Czech Republic.
     */
    CS_CZ("cs-CZ", "cs"),
    /**
     * The default dialect of Danish is from Denmark.
     */
    DA_DK("da-DK", "da"),
    /**
     * The default dialect of German is from Germany.
     */
    DE_DE("de-DE", "de"),
    /**
     * The default dialect of Greek is from Greece.
     */
    EL_GR("el-GR", "el"),
    /**
     * Canadian English
     */
    EN_CA("en-CA", "en-CA"),
    /**
     * British English
     */
    EN_GB("en-GB", "en-GB"),
    /**
     * Pirate English (esoteric)
     */
    EN_PT("en-PT", "en-PT"),
    /**
     * Upside-down English (esoteric)
     */
    EN_UD("en-UD", "en-UD"),
    /**
     * American English
     */
    EN_US("en-US", "en-US"),
    /**
     * Spaniard Spanish 
     */
    ES_ES("es-ES", "es-ES"),
    /**
     * The default dialect of Finnish is from Finland.
     */
    FI_FI("fi-FI", "fi"),
    /**
     * The default dialect of French is from France.
     */
    FR_FR("fr-FR", "fr"),
    /**
     * The default dialect of Hebrew is from Israel.
     */
    HE_IL("he-IL", "he"),
    /**
     * The default dialect of Hungarian is from Hungary.
     */
    HU_HU("hu-HU", "hu"),
    /**
     * The default dialect of Italian is from Italy.
     */
    IT_IT("it-IT", "it"),
    /**
     * The default dialect of Japanese is from Japan.
     */
    JA_JP("ja-JP", "ja"),
    /**
     * The default dialect of Korean is from South Korea.
     */
    KO_KR("ko-KR", "ko"),
    /**
     * The default dialect of Lolcat is from the United States.
     * (esoteric)
     */
    LOL_US("lol-US", "lol"),
    /**
     * The default dialect of Bokmal is from Norway.
     */
    NB_NO("nb-NO", "nb"),
    /**
     * The default dialect of Dutch is from the Netherlands.
     */
    NL_NL("nl-NL", "nl"),
    /**
     * Norwegian Nynorsk.
     */
    NN_NO("nn-NO", "nn-NO"),
    /**
     * The default dialect of Polish is from Poland.
     */
    PL_PL("pl-PL", "pl"),
    /**
     * Brazilian Portuguese.
     */
    PT_BR("pt-BR", "pt-BR"),
    /**
     * Portugal Portuguese.
     */
    PT_PT("pt-PT", "pt-PT"),
    /**
     * The default dialect of Romanian is from Romania.
     */
    RO_RO("ro-RO", "ro"),
    /**
     * The default dialect of Russian is from Russia.
     */
    RU_RU("ru-RU", "ru"),
    /**
     * The default dialect of Serbian is from Serbia and Montenegro.
     */
    SR_SP("sr-SP", "sr"),
    /**
     * Sweden Swedish.
     */
    SV_SE("sv-SE", "sv-SE"),
    /**
     * The default dialect of Turkish is from Turkey.
     */
    TR_TR("tr-TR", "tr"),
    /**
     * The default dialect of Ukrainian is from Ukraine
     */
    UK_UA("uk-UA", "uk"),
    /**
     * The default dialect of Vietnamese is from Viet Nam.
     */
    VI_VN("vi-VN", "vi"),
    /**
     * China Chinese
     */
    ZH_CN("zh-CN", "zh-CN"),
    /**
     * Taiwanese Chinese
     */
    ZH_TW("zh-TW", "zh-TW");

    private final String languageCode;
    private final String languageFolder;

    /**
     * Instantiates a new language
     *
     * @param languageCode   <p>The language code for the new language</p>
     * @param languageFolder <p>The name of the language's relative folder containing all its language files</p>
     */
    Language(String languageCode, String languageFolder) {
        this.languageCode = languageCode;
        this.languageFolder = languageFolder;
    }

    /**
     * Gets whether a given language specification matches this language
     *
     * @param language <p>The language specified by a user</p>
     * @return <p>True if the specified language matches this language</p>
     */
    public boolean matches(String language) {
        return this.languageCode.equalsIgnoreCase(language);
    }

    /**
     * Gets the language of this language code
     *
     * @return <p>The language of this language code</p>
     */
    public String getLanguage() {
        return this.languageCode.split("-")[0];
    }

    /**
     * Gets the language code of this language
     *
     * @return <p>The language code of this language</p>
     */
    public String getLanguageCode() {
        return this.languageCode;
    }

    /**
     * Gets the relative language folder containing this language's files
     *
     * @return <p>The relative language folder</p>
     */
    public String getLanguageFolder() {
        return this.languageFolder;
    }

}
