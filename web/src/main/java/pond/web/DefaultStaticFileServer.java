package pond.web;

import pond.common.Convert;
import pond.common.PATH;
import pond.common.S;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultStaticFileServer implements StaticFileServer {

  //TODO read from config
  static final String HTTP_DATE_FORMAT = S.avoidNull(
      S.config.get(StaticFileServer.class, StaticFileServer.HTTP_DATE_FORMAT)
      , "EEE, dd MMM yyyy HH:mm:ss zzz");

  static final String HTTP_DATE_GMT_TIMEZONE = S.avoidNull(
      S.config.get(StaticFileServer.HTTP_DATE_GMT_TIMEZONE), "GMT");

  static final int HTTP_CACHE_SECONDS =
      Convert.toInt(
          S.avoidNull(
              S.config.get(StaticFileServer.class, StaticFileServer.HTTP_CACHE_SECONDS)
              , "60")
      );

  boolean config_enable_redirect = false;

  File root;

  public DefaultStaticFileServer() {}

  @Override
  public StaticFileServer watch(String dir) {
    S._assert(dir);
    String _root = PATH.isAbsolute(dir) ?
        dir
        :
        S.config.get(Pond.class, Pond.CONFIG_WEB_ROOT).concat(File.separator).concat(dir);
    root = new File(_root);

    if (!root.exists() || !root.canRead())
      throw new RuntimeException("Invalid static file server root : " + _root);

    return this;
  }

  @Override
  public StaticFileServer enableRedirect() {
    this.config_enable_redirect = true;
    return this;
  }

  private boolean allowList() {
    return Boolean.parseBoolean(S.config.get(StaticFileServer.ALLOW_LISTING_DIR));
  }


  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

  /**
   */
  private String sanitizeUri(String uri, Route route) {

    if (uri.isEmpty() || uri.charAt(0) != '/') {
      //bad path
      return null;
    }

    // Simplistic dumb security check.
    // You will have to do something serious in the production environment.
    // FIXME
    if (uri.contains(File.separator + '.') ||
        uri.contains('.' + File.separator) ||
        uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
        INSECURE_URI.matcher(uri).matches()) {
      return null;
    }

    if (route != null) {

      String def = route.defPath().pattern();

      Matcher matcher = Pattern.compile(def).matcher(uri);

      if(matcher.find()){
        uri = "/" + uri.substring(matcher.start(1));
      }

      //String prefix = any.substring(0, any.lastIndexOf("/"));
    }

    // Convert file separators.
    uri = uri.replace('/', File.separatorChar);

    // Convert to absolute path.
    return root.getAbsoluteFile() + uri;
  }


  @Override
  public void apply(Request request, Response response) {
    if (!"get".equalsIgnoreCase(request.method())) {
      response.sendError(405, "Method Not Allowed");
      return;
    }
    String path = request.path();
    String absPath = sanitizeUri(path, request.ctx().route());

    S._debug(logger, log -> log.debug("Abs_Path: " + absPath));

    if (absPath == null) {
      response.sendError(403, "Forbidden[ Request Uri " + path + " is illegal]");
      return;
    }

    File file = new File(absPath);

    if (file.isHidden() || !file.exists()) {
      //do not handle since this is a express
//      response.sendError(404, "Not Found");
      return;
    }

    if (file.isDirectory()) {
      if (path.endsWith("/")) {
        String indexFileName = S.avoidNull(S.config.get(StaticFileServer.INDEX_FILE), "index.html");

        File index = new File(file, indexFileName);

        if (!index.isHidden() && index.exists()) {
          if(config_enable_redirect)
            response.redirect(indexFileName);
          else
            response.sendFile(index);
          return;
        } else {
          if (allowList()) sendListing(response, file);
          else {
//            response.send(404);
            return;
          }
        }

      } else {
        response.redirect(path + "/");
      }
      return;
    }

    if (!file.isFile()) {
      response.sendError(403, "Forbidden[ Request Uri " + path + " is not a file ]");
      return;
    }

    // Cache Validation
    String ifModifiedSince = request.header("If-Modified-Since");
    if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
      SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
      Date ifModifiedSinceDate = S._try_ret(() -> dateFormatter.parse(ifModifiedSince));

      // Only compare up to the second because the datetime format we send to the client
      // does not have milliseconds
      long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
      long fileLastModifiedSeconds = file.lastModified() / 1000;
      if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
        setDateHeader(response);
        response.send(304);
        return;
      }
    }


    setDateAndCacheHeaders(response, file);

    //TODO Range
//        //Range
//        if (request.header("range") != null){
//            response.header("accept-ranges","bytes");
//            response.header("content-length", String.valueOf(file.length()));
//            response.send(200);
//            return;
//        }

    //TODO ETag

    response.sendFile(file);

    //keep-alive head process
  }

  private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

  private static void sendListing(Response resp, File dir) {
    resp.contentType("text/html; charset=UTF-8");

    String dirPath = dir.getPath();
    PrintWriter buf = resp.writer()
        .append("<!DOCTYPE html>\r\n")
        .append("<html><head><title>")
        .append("Listing handle: ")
        .append(dirPath)
        .append("</title></head><body>\r\n")

        .append("<h3>Listing handle: ")
        .append(dirPath)
        .append("</h3>\r\n")

        .append("<ul>")
        .append("<li><a href=\"../\">..</a></li>\r\n");

    File[] files = dir.listFiles();
    S._assert(files);
    for (File f : files) {
      if (f.isHidden() || !f.canRead()) {
        continue;
      }

      String name = f.getName();
      if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
        continue;
      }

      buf.append("<li><a href=\"")
          .append(name)
          .append("\">")
          .append(name)
          .append("</a></li>\r\n");
    }

    buf.append("</ul></body></html>\r\n");
    buf.flush();
    resp.send(200);
  }

  private static void setDateHeader(Response response) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    Calendar time = new GregorianCalendar();
    response.header("date", dateFormatter.format(time.getTime()));
  }

  private static void setDateAndCacheHeaders(Response response, File fileToCache) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    // date header
    Calendar time = new GregorianCalendar();
    response.header("date", dateFormatter.format(time.getTime()));

    // Add cache headers
    time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    response.header("Expires", dateFormatter.format(time.getTime()));
    response.header("Cache-Control", "private, max-age=" + HTTP_CACHE_SECONDS);
    response.header("Last-Modified", dateFormatter.format(new Date(fileToCache.lastModified())));
  }


//    private static int[] parseRange(String range,)
}
