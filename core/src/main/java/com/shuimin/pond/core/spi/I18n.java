package com.shuimin.pond.core.spi;

import static com.shuimin.pond.core.Pond.CUR;

/**
 * Created by ed on 7/1/14.
 */
public interface I18n {
    public static final String MESSAGE_BUNDLE_PREFIX = "message";
    public static final String MESSAGE_BUNDLE_SUFFIX = "bundle";
    public static final String CUR_LOCALE_STRING = "cur_locale";

    /**
     * Get message from default message bundle
     * located at :classpath/message.bundle by default
     *
     * @param msg msg key
     * @return msg content
     */
    String msg(String msg);

    /**
     * Get message from localized message bundle
     * i.e. :classpath/message_zh_CN.bundle
     *
     * @param msg    msg key
     * @param locale locale symbol i.e. zh_CN en_US
     * @return msg content
     */
    String msg(String msg, String locale);

    public default void setCurLocale(String locale) {
        CUR().attr(CUR_LOCALE_STRING, locale);
    }
}
