package pond.web;

import pond.common.PATH;
import pond.common.S;
import pond.web.spi.BaseServer;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


class DefaultStaticFileServer implements Mid {

  //TODO read from config
  static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
  static final int HTTP_CACHE_SECONDS = 60;

  File root;

  DefaultStaticFileServer(String dir) {
    String _root = PATH.isAbsolute(dir) ? dir : PATH.detectWebRootPath().concat(File.separator).concat(dir);
    root = new File(_root);
    if (!root.exists() || !root.canRead())
      throw new RuntimeException("Invalid static file server root : " + _root);
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
      String def = route.def_path;
      String prefix = def.substring(0, def.lastIndexOf("/"));
      uri = uri.substring(prefix.length());
      S.echo(uri);
    }


//        //cut the prefix
//        if (def != null) {
//            Matcher matcher = def.matcher(uri);
//            if (matcher.matches()) {
//                String result = matcher.group();
//                S.echo(result);
//                uri = result;
//            }
//        }

    // Convert file separators.
    uri = uri.replace('/', File.separatorChar);

    // Convert to absolute path.
    return root.getAbsoluteFile() + File.separator + uri;
  }


  @Override
  public void apply(Request request, Response response) {
    if (!"get".equalsIgnoreCase(request.method())) {
      response.sendError(405, "Method Not Allowed");
      return;
    }
    String path = request.path();
    String absPath = sanitizeUri(path, request.ctx().route);

    S._debug(BaseServer.logger, log -> log.debug("Abs_Path: " + absPath));

    if (absPath == null) {
      response.sendError(403, "Forbidden[ Request Uri " + path + " is illegal]");
      return;
    }

    File file = new File(absPath);
    if (file.isHidden() || !file.exists()) {
      response.sendError(404, "Not Found");
      return;
    }

    if (file.isDirectory()) {
      if (path.endsWith("/")) {
        sendListing(response, file);
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
        .append("Listing of: ")
        .append(dirPath)
        .append("</title></head><body>\r\n")

        .append("<h3>Listing of: ")
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
    response.header("Date", dateFormatter.format(time.getTime()));
  }

  private static void setDateAndCacheHeaders(Response response, File fileToCache) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    // Date header
    Calendar time = new GregorianCalendar();
    response.header("Date", dateFormatter.format(time.getTime()));

    // Add cache headers
    time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    response.header("Expires", dateFormatter.format(time.getTime()));
    response.header("Cache-Control", "private, max-age=" + HTTP_CACHE_SECONDS);
    response.header("Last-Modified", dateFormatter.format(new Date(fileToCache.lastModified())));
  }


//    private static int[] parseRange(String range,)
}
