package org.sgrewritten.stargate.formatting;

/**
 * A representation of all potentially available languages
 */
public enum Language {

    AF_ZA("af-ZA", "af"),
    AR_SA("ar-SA", "ar"),
    CA_ES("ca-ES", "ca"),
    CS_CZ("cs-CZ", "cs"),
    DA_DK("da-DK", "da"),
    DE_DE("de-DE", "de"),
    EL_GR("el-GR", "el"),
    EN_CA("en-CA", "en-CA"),
    EN_GB("en-GB", "en-GB"),
    EN_PT("en-PT", "en-PT"),
    EN_UD("en-UD", "en-UD"),
    EN_US("en-US", "en-US"),
    ES_ES("es-ES", "es-ES"),
    FI_FI("fi-FI", "fi"),
    FR_FR("fr-FR", "fr"),
    HE_IL("he-IL", "he"),
    HU_HU("hu-HU", "hu"),
    IT_IT("it-IT", "it"),
    JA_JP("ja-JP", "ja"),
    KO_KR("ko-KR", "ko"),
    LOL_US("lol-US", "lol"),
    NB_NO("nb-NO", "nb"),
    NL_NL("nl-NL", "nl"),
    NN_NO("nn-NO", "nn-NO"),
    PL_PL("pl-PL", "pl"),
    PT_BR("pt-BR", "pt-BR"),
    PT_PT("pt-PT", "pt-PT"),
    RO_RO("ro-RO", "ro"),
    RU_RU("ru-RU", "ru"),
    SR_SP("sr-SP", "sr"),
    SV_SE("sv-SE", "sv-SE"),
    TR_TR("tr-TR", "tr"),
    UK_UA("uk-UA", "uk"),
    VI_VN("vi-VN", "vi"),
    ZH_CN("zh-CN", "zh-CN"),
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
