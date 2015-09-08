package pond.web.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.web.Mid;

public interface StaticFileServer extends Mid {

  static Logger logger = LoggerFactory.getLogger(StaticFileServer.class);

  StaticFileServer watch(String relative);

}
