package pond.core.spi.server;

import pond.common.S;
import pond.common.abs.Makeable;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.*;
import pond.core.exception.UnexpectedException;
import pond.core.http.HttpMethod;
import pond.core.misc.HSRequestWrapper;
import pond.core.misc.HSResponseWrapper;
import pond.core.misc.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.core.spi.BaseServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static pond.common.S.dump;
import static pond.core.Pond.debug;

/**
 * Created by ed on 2014/4/10.
 * <p>A Simple static attachment server</p>
 */

@Deprecated // redirection problem
public class DefaultFileServer
        implements Makeable<DefaultFileServer>,BaseServer.StaticFileServer, Mid{

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    private static final Pattern INSECURE_PATH = Pattern.compile(".*[<>&\"].*");
    public String publicDir;
    static Logger logger = LoggerFactory.getLogger(DefaultFileServer.class);
    /**
     * Provider
     */

    private Function.F0<Pattern> allowedFileNamesProvider =
            () -> Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
    private String[] defaultPages = {"index.html"};
    private String charset = "utf-8";
    private boolean allowListDir = false;

    private Callback.C2<Response, File> listDir = this::defaultListFiles;

    public DefaultFileServer(Pond p, String str) {
        String absPath = p.pathRelWebRoot(str);

        File f = new File(absPath);
        if (!f.exists() || !f.canWrite()) {
            throw new UnexpectedException() {
                @Override
                public String brief() {
                    return "File[" + absPath + "] not valid";
                }
            };
        }
        debug("static server path : " + absPath);
        publicDir = absPath;
    }

    public DefaultFileServer charset(String charset) {
        this.charset = charset;
        return this;
    }

    public DefaultFileServer allowedNames(Function.F0<Pattern> f) {
        this.allowedFileNamesProvider = f;
        return this;
    }

    public DefaultFileServer defaultPages(String... x) {
        this.defaultPages = x;
        return this;
    }

    public DefaultFileServer onList(Callback.C2<Response, File> f) {
        this.listDir = f;
        return this;
    }

    private String findDefaultPage(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new NullPointerException();
        }
        String[] names = defaultPages;
        for (File f : files) {
            for (String name : names) {
                if (name.equals(f.getName())) {
                    return name;
                }
            }
        }
        return null;
    }

    private void handle(Request req, Response resp) {
        if (HttpMethod.of(req.method()) != HttpMethod.GET) {
            resp.sendError(406,"");
            return;
        }
        final String path = req.path();
        final String absPath = validPath(path);

        if (S.str.isBlank(absPath)) {
            resp.sendError(403,"");
            return;
        }

        File file = new File(absPath);

        Pattern allowedNames = this.allowedFileNamesProvider.apply();


        if (!allowedNames.matcher(file.getName()).matches()) {
            resp.sendError(403,"");
            return;
        }

        if (file.isHidden() || !file.exists()) {
            resp.sendError(404,"");
            return;
        }

        if (file.isDirectory()) {
            if (path.endsWith("/")) {
                String tryDefaultPage = findDefaultPage(file);
                if (tryDefaultPage != null) {
                    resp.redirect(path + tryDefaultPage);
                    return;
                }
                //do listing
                if(this.allowListDir)
                    listDir.apply(resp, file);
            } else {
                resp.redirect(path + '/');
            }
            return;
        }

        if (!file.isFile()) {
            resp.sendError(403,"");
            return;
        }

        setContentLength(resp, file.length());

        setContentType(resp, file);        //cache

        // Cache Validation
        String[] strings = req.headers().get("If-Modified-Since");
        if (strings != null && strings.length > 0) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate;
            try {
                ifModifiedSinceDate = dateFormatter.parse(strings[0]);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                resp.send(304);//not modified
                return;
            }
        }


        setDateAndCacheHeaders(resp, file);

        resp.render(Render.file(file));

    }

    public void defaultListFiles(Response resp, File dir) {
        resp.contentType("text/html; charset=UTF-8");

        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath()
                .replace(publicDir, "\\").replaceAll("\\\\", "/");

        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append("Listing of: ");
        buf.append(dirPath);
        buf.append("</title></head><body>\r\n");

        buf.append("<h3>Listing of: ");
        buf.append(dirPath);
        buf.append("</h3>\r\n");

        buf.append("<ul>");
        buf.append("<li><a href=\"../\">..</a></li>\r\n");

        Pattern allowedNames = this.allowedFileNamesProvider.apply();

        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }

            String name = f.getName();
            if (!allowedNames.matcher(name).matches()) {
                continue;
            }

            buf.append("<li><a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");
        resp.write(buf.toString());
        resp.send(200);
    }

    private String validPath(String path) {
        if (!path.startsWith("/")) {
            return null;
        }
        String _path = path.replace('/', File.separatorChar);

        if (_path.contains(File.separator + '.') ||
                _path.contains('.' + File.separator) ||
                _path.startsWith(".") || _path.endsWith(".") ||
                INSECURE_PATH.matcher(_path).matches()) {
            return null;
        }

        return publicDir + File.separator + _path;
    }


    private void setContentLength(Response resp, long length) {
        resp.header("Content-Length", String.valueOf(length));
    }


    private void setDateAndCacheHeaders(Response resp, File toCache) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        resp.header("Date", dateFormat.format(time.getTime()));

        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        resp.header("Expires", dateFormat.format(time.getTime()));
        resp.header("Cache-Control", "private, max-age=" + HTTP_CACHE_SECONDS);
        resp.header("Last-Modified", dateFormat.format(new Date(toCache.lastModified())));
    }


    private void setContentType(Response resp, File file) {
        String[] fullName = file.getName().split("\\.");
        logger.debug(file.getAbsolutePath() + " suffix " + dump(fullName));
        String ext = fullName[fullName.length - 1];
        resp.contentType(MimeTypes.getMimeType(ext) + ";charset=" + charset);
    }

    @Override
    public void apply(Request request, Response response, Callback.C0 c0) {
        this.handle(request,response);
//        c0.apply();
    }

    public void handle(HttpServletRequest req, HttpServletResponse resp) {
        this.handle(new HSRequestWrapper(req), new HSResponseWrapper(resp));
    }

    @Override
    public BaseServer.StaticFileServer allowList(boolean b) {
        this.allowListDir = b;
        return this;
    }

    @Override
    public BaseServer.StaticFileServer welcomeFiles(String... files) {
        return this.defaultPages(files);
    }
}
