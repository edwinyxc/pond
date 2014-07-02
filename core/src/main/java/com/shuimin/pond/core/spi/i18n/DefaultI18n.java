package com.shuimin.pond.core.spi.i18n;

import com.shuimin.common.S;
import com.shuimin.pond.core.spi.I18n;
import com.shuimin.pond.core.spi.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.shuimin.pond.core.Pond.CUR;

/**
 * Created by ed on 7/1/14.
 */
public class DefaultI18n implements I18n {
    Charset charset = Charset.forName("UTF-8");
    Map<String, Properties> msg_bundles = new HashMap<>();

    Logger logger = Logger.createLogger(I18n.class);

    private Properties load_msg_bundle(String name) {
        try (InputStream in =
                     System.class.getResourceAsStream(
                             I18n.MESSAGE_BUNDLE_PREFIX
                                     + (S.str.isBlank(name) ?
                                     null
                                     : ("_" + name))
                             +"."+I18n.MESSAGE_BUNDLE_SUFFIX
                     )) {
            if (in == null) {
                logger.fatal("msg_bundle:" + name + "not found.");
                throw new RuntimeException("msg_bundle:" + name + "not found.");
            }
            Properties ret = new Properties();
            ret.load(new InputStreamReader(in, charset));
            return ret;
        } catch (IOException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Properties get_msg_bundle(String name) {
        Properties ret;
        if ((ret = msg_bundles.get(name)) == null) {
            ret = load_msg_bundle(name);
            msg_bundles.put(name, ret);
        }
        return ret;
    }

    @Override
    public String msg(String msg) {
        String locale = (String) CUR().attr(CUR_LOCALE_STRING);
        return get_msg_bundle(locale).getProperty(msg);
    }

    @Override
    public String msg(String msg, String locale) {
        return get_msg_bundle(locale).getProperty(msg);
    }
}
