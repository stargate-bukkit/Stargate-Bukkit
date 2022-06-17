package net.TheDgtl.Stargate.formatting;

import net.TheDgtl.Stargate.Stargate;

import java.util.Map;

/**
 * A representation of all potentially available languages
 */
public enum Language {

    AF_ZA("af-ZA"),
    AR_SA("ar-SA"),
    CA_ES("ca-ES"),
    CS_CZ("cs-CZ"),
    DA_DK("da-DK"),
    DE_DE("de-DE"),
    EL_GR("el-GR"),
    EN_CA("en-CA"),
    EN_GB("en-GB"),
    EN_PT("en-PT"),
    EN_UD("en-UD"),
    EN_US("en-US"),
    ES_ES("es-ES"),
    FI_FI("fi-FI"),
    FR_FR("fr-FR"),
    HE_IL("he-IL"),
    HU_HU("hu-HU"),
    IT_IT("it-IT"),
    JA_JP("ja-JP"),
    KO_KR("ko-KR"),
    LOL_US("lol-US"),
    NB_NO("nb-NO"),
    NL_NL("nl-NL"),
    NN_NO("nn-NO"),
    PL_PL("pl-PL"),
    PT_BR("pt-BR"),
    PT_PT("pt-PT"),
    RO_RO("ro-RO"),
    RU_RU("ru-RU"),
    SR_SP("sr-SP"),
    SV_SE("sv-SE"),
    TR_TR("tr-TR"),
    UK_UA("uk-UA"),
    VI_VN("vi-VN"),
    ZH_CN("zh-CN"),
    ZH_TW("zh-TW");

    private final String languageCode;
    private final String languageShorthand;

    /**
     * Instantiates a new language
     *
     * @param languageCode <p>The language code for the new language</p>
     */
    Language(String languageCode) {
        this.languageCode = languageCode;
        Map<String, String> languageShorthands = Stargate.getInstance().getLanguageManager().getLanguageShorthands();
        languageShorthand = getKeyFromValue(languageShorthands, languageCode);
    }

    /**
     * Gets whether a given language specification matches this language
     *
     * @param language <p>The language specified by a user</p>
     * @return <p>True if the specified language matches this language</p>
     */
    public boolean matches(String language) {
        return this.languageShorthand.equalsIgnoreCase(language) ||
                this.languageCode.equalsIgnoreCase(language);
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
     * Gets the key for the given value
     *
     * @param map   <p>The map to search</p>
     * @param value <p>The value to search for</p>
     * @return <p>The key mapping to the given value</p>
     */
    private String getKeyFromValue(Map<String, String> map, String value) {
        for (String key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

}
