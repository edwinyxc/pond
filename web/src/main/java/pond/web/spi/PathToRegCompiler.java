package pond.web.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Tuple;
import pond.web.Route;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SPI -- used for compile an arbitrary string to a much more meaningful regular expression.
 * This is useful as a in-url-params binding tool at the configuration layer.
 */
public interface PathToRegCompiler {

    final static Logger logger = LoggerFactory.getLogger(PathToRegCompiler.class);

    PreCompiledPath compile(String path);

    String preparePath(Route route, String path);

}
