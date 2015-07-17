package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.core.Request;
import pond.core.http.HttpUtils;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettyReqWrapper implements Request {

    final static Logger logger = LoggerFactory.getLogger(NettyReqWrapper.class);

    NettyHttpServer server;
    HttpRequest n_req;

    ByteBuf content;

    HttpHeaders n_headers;
    final Channel channel;
    final QueryStringDecoder uriDecoder;
    final Map<String, List<String>> headers = new HashMap<>();
    //merge attrs without replacing the origin
    final Map<String, List<String>> params = new HashMap<>();
    final Map<String, List<String>> attrs = new HashMap<>();
    final Map<String, List<UploadFile>> uploads = new HashMap<>();
    final Map<String, Cookie> cookies = new HashMap<>();

    class NettyUploadFile implements Request.UploadFile {
        FileUpload file;

        NettyUploadFile(FileUpload nettyUpload) {
            file = nettyUpload;
        }

        @Override
        public String name() {
            return file.getName();
        }

        @Override
        public String filename() {
            return file.getFilename();
        }

        @Override
        public InputStream inputStream() throws IOException {
            return new ByteBufInputStream(file.getByteBuf()){
                @Override
                public void close() throws IOException {
                    super.close();
                    if(file.refCnt() > 0) file.release();
                }
            };
        }

        @Override
        public File file() throws IOException {
            return file.getFile();
        }
    }


    public NettyReqWrapper(ChannelHandlerContext ctx,
                           HttpRequest req,
                           NettyHttpServer server,
                           List<Attribute> parsedAttributes,
                           List<FileUpload> fileUploads
    ) {

        this.server = server;
        channel = ctx.channel();
        n_req = req;
        n_headers = req.headers();
        uriDecoder = new QueryStringDecoder(n_req.uri());

        S._for(parsedAttributes).each(attr -> {
            String name = attr.getName();
            String value = S._try_ret(attr::getValue);

            HttpUtils.appendToMap(attrs, name, value);
        });

        S._for(fileUploads).each(fileUpload -> {
            String name = fileUpload.getName();
            HttpUtils.appendToMap(uploads, name, new NettyUploadFile(fileUpload));
        });

    }


    public NettyReqWrapper(ChannelHandlerContext ctx,
                           HttpRequest req,
                           NettyHttpServer server
    ) {
        this(ctx, req, server, null, null);
    }

    NettyReqWrapper init() {
        long _parse_start = S.now();
        parseHeaders();
        parseCookies();
        parseQueries();
        S._debug(logger, log -> log.debug("parse_time:" + (S.now() - _parse_start)));
        return this;
    }

    void parseHeaders() {
        S._for(n_headers.names()).each(name -> HttpUtils.appendToMap(headers, name.toString(), n_headers.getAndConvert(name)));
    }

    void parseQueries() {
        //parse query strings
        params.putAll(S._for(uriDecoder.parameters()).val());
    }

    void parseCookies() {
        //parse cookie
        String cookieString = String.valueOf(S.avoidNull(n_headers.get(HttpHeaderNames.COOKIE), ""));
        if (S.str.notBlank(cookieString)) {
            java.util.Set<io.netty.handler.codec.http.Cookie> decodedCookies = ServerCookieDecoder.decode(cookieString);
            S._for(decodedCookies).map(c -> {
                Cookie ret = new Cookie(c.name(), c.value());
                ret.setComment(c.comment());
                if (S.str.notBlank(c.domain())) ret.setDomain(c.domain());
                ret.setHttpOnly(c.isHttpOnly());
                ret.setMaxAge((int) c.maxAge());
                ret.setSecure(c.isSecure());
                ret.setPath(c.path());
                ret.setVersion(c.version());
                return ret;
            }).each(cookie -> cookies.put(cookie.getName(), cookie));
        }

    }

    private String fullUri() {
        AsciiString protocol = HttpVersion.HTTP_1_1.protocolName();

        String hostName = ((InetSocketAddress) channel.localAddress()).getHostName();

        //IPv6 workaround
        if (hostName.contains(":")) { /*an ipv6*/
            hostName = "[" + hostName + "]";
        }

        String port = String.valueOf(((InetSocketAddress) channel.localAddress()).getPort());
        return protocol + "://" + hostName + ":" + port + n_req.uri();
    }

    @Override
    public String uri() {
        return fullUri();
    }

//    @Override
//    public Locale locale() {
//        String sc = S.avoidNull(n_headers.get(HttpHeaderNames.ACCEPT_LANGUAGE), "").toString();
//        String[] parsed;
//        if (S.str.isBlank(sc)) {
//            sc = (String) S.avoidNull(server.env(BaseServer.LOCALE), "zh_CN");
//        }
//        if ((parsed = sc.split("-")).length >= 2) {
//            return new Locale(parsed[0], parsed[1]);
//        } else return new Locale("en", "US");
//    }

    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

    @Override
    public Map<String, List<String>> params() {
        return params;
    }

    @Override
    public Map<String, List<UploadFile>> files() {
        return uploads;
    }

    @Override
    public Map<String, List<String>> attrs() {
        return attrs;
    }

    @Override
    public String method() {
        return n_req.method().toString();
    }

    @Override
    public String remoteIp() {
        CharSequence ip = n_headers.get("x-forwarded-for");
        if (ip == null) return null;
        if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
            ip = n_headers.get("Proxy-Client-IP");
        }
        if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
            ip = n_headers.get("WL-Proxy-Client-IP");
        }
        if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
            ip = channel.remoteAddress().toString();
        }
        return String.valueOf(ip);
    }

    @Override
    public Map<String, Cookie> cookies() {
        return cookies;
    }

    @Override
    public String characterEncoding() {
        throw new UnsupportedOperationException("not allowed in netty");
    }
}
