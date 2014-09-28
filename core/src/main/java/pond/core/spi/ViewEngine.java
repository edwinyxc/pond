package pond.core.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ed on 2014/5/8.
 */
public interface ViewEngine {
    static Logger logger = LoggerFactory.getLogger(ViewEngine.class);

    void configViewPath(String path);

    void render(OutputStream out,
                String relativePath,
                Object data) throws IOException;
}
