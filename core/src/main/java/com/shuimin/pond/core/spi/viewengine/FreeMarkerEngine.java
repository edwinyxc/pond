package com.shuimin.pond.core.spi.viewengine;

import com.shuimin.common.S;
import com.shuimin.pond.core.Global;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.spi.Logger;
import com.shuimin.pond.core.spi.ViewEngine;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.Charset;

import static com.shuimin.pond.core.Pond.debug;

/**
 * Created by ed on 2014/5/8.
 */
public class FreeMarkerEngine implements ViewEngine {
    final Charset utf8 = Charset.forName("UTF-8");
    final Logger logger = Logger.createLogger(FreeMarkerEngine.class);
    Configuration cfg = new Configuration();
    {
        File f = new File((String) Pond.config(Global.TEMPLATE_PATH));
        logger.info("find tmpl" + f.getAbsolutePath());
        try {
            cfg.setDirectoryForTemplateLoading(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(OutputStream out, String relativePath, Object map) {
        Writer writer = new OutputStreamWriter(out, utf8);
        try {
            Template t = this.cfg.getTemplate(relativePath);
            t.process(map, writer);
        } catch (IOException e) {
            S._throw(e);
        } catch (TemplateException e) {
            e.printStackTrace();
        }

    }
}
