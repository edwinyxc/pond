package com.shuimin.pond.core.spi;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ed on 2014/5/8.
 */
public interface ViewEngine {

    void configViewPath(String path);

    void render(OutputStream out,
                String relativePath,
                Object data) throws IOException;
}
