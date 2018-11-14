package pond.web.fileserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;

/**
 * !!!Important, for test purpose only, do not use it in production environments
 */
public interface StaticFileServer extends CtxHandler<HttpCtx> {

  String HTTP_DATE_FORMAT = "http_date_format";
  String HTTP_DATE_GMT_TIMEZONE = "http_date_gmt_timezone";
  String HTTP_CACHE_SECONDS = "http_cache_seconds";
  String ALLOW_LISTING_DIR = "allow_listing_dir";
  String INDEX_FILE = "index_file";

  Logger logger = LoggerFactory.getLogger(StaticFileServer.class);

}
