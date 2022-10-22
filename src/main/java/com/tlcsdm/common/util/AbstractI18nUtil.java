package com.tlcsdm.common.util;

import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 可以通过 -Dnl=en/zh/ja 来选择语言，默认是系统语言
 * BASENAME 是资源包路径，可以通过-Dnlurl=xxx 指定
 */
public abstract class AbstractI18nUtil {

    private Locale locale;

    //模块名
    protected String module = "";
    /**
     * 资源包默认路径
     */
    private String baseName = module + (module.length() > 0 ? "." : module) + "i18n.messages";

    private List<Locale> supportLocale = Lists.newArrayList(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE);

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     */
    public Locale getDefaultLocale() {
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
        Locale locale = Locale.getDefault();
        if (supportLocale.contains(locale)) {
            return locale;
        }
        return Locale.ENGLISH;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key  message key
     * @param args optional arguments for the message
     * @return localized formatted string
     */
    public String get(final String key, final Object... args) {
        if (locale == null) {
            initLocale();
        }
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    private void initLocale() {
        locale = getDefaultLocale();
        if (System.getProperty("nlurl") != null) {
            baseName = System.getProperty("nlurl");
        }
    }
}
