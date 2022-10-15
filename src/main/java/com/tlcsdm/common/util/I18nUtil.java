package com.tlcsdm.common.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 可以通过 -Dnl=en/zh/ja 来选择语言，默认是系统语言
 * BASENAME 是资源包路径，可以通过-Dnlurl=xxx 指定
 */
public class I18nUtil {

    /**
     * the current selected Locale.
     */
    private static final Locale locale;
    /**
     * 资源包默认路径
     */
    public static String BASENAME = "i18n.messages";

    static {
        locale = getDefaultLocale();
        if (System.getProperty("nlurl") != null) {
            BASENAME = System.getProperty("nlurl");
        }
    }

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     */
    public static Locale getDefaultLocale() {
        String lang = System.getProperty("nl");
        if (lang != null) {
            switch (lang.toLowerCase()) {
                case "en":
                    return Locale.ENGLISH;
                case "zh":
                    return Locale.SIMPLIFIED_CHINESE;
                case "ja":
                    return Locale.JAPANESE;
                default:
                    return Locale.ENGLISH;
            }
        }
        return Locale.ENGLISH;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        locale = locale;
    }

    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key  message key
     * @param args optional arguments for the message
     * @return localized formatted string
     */
    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle(BASENAME, getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }
}
