package com.shuimin.pond.core.spi.viewengine;

import com.shuimin.pond.core.spi.Logger;
import com.shuimin.pond.core.spi.ViewEngine;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.Charset;


/**
 * Created by ed on 2014/5/8.
 */
public class FreeMarkerEngine implements ViewEngine {
    final Charset utf8 = Charset.forName("UTF-8");
    final Logger logger = Logger.createLogger(FreeMarkerEngine.class);

    Configuration cfg = new Configuration();

    @Override
    public void configViewPath(String path) {
        File f = new File(path);
        logger.info("find tmpl" + f.getAbsolutePath());
        try {
            cfg.setDirectoryForTemplateLoading(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(OutputStream out, String relativePath, Object map)
            throws IOException {
        try (Writer writer = new OutputStreamWriter(out, utf8)) {
            Template t = this.cfg.getTemplate(relativePath);
            t.process(map, writer);

        } catch (TemplateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
