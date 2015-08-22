package pond.web.spi;

import pond.web.Mid;

public interface StaticFileServer extends Mid {

  StaticFileServer watch(String relative);

}
