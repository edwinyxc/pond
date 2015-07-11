package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.core.AbstractRequest;
import pond.core.spi.BaseServer;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class NettyReqWrapper extends AbstractRequest {

    final static Logger logger = LoggerFactory.getLogger(NettyReqWrapper.class);

    NettyHttpServer server;
    FullHttpRequest n_req;

    HttpHeaders n_headers;
    Channel channel;
    QueryStringDecoder uriDecoder;
    Map<String, String[]> headers = new HashMap<>();
    Map<String, String[]> params = new HashMap<>();
    Iterable<Cookie> cookies;

    HttpPostRequestDecoder postRequestDecoder;

    //multipart configuration
    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
    //useful for upload
    private HttpData partialContent;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    public NettyReqWrapper(ChannelHandlerContext ctx, FullHttpRequest req, NettyHttpServer server) {

        //check bad request
        long _parse_start = S.now();
        this.server = server;
        channel = ctx.channel();
        n_req = req;
        n_headers = req.headers();
        HttpMethod method = req.method();
        uriDecoder = new QueryStringDecoder(n_req.uri());

        //parse headers
        S._for(n_headers.names()).each(name ->
                        headers.put(name.toString(), S._for(n_headers.getAllAndConvert(name)).join())
        );

        //parse cookie
        String cookieString = String.valueOf(S.avoidNull(n_headers.get(HttpHeaderNames.COOKIE), ""));
        if (S.str.notBlank(cookieString)) {
            java.util.Set<io.netty.handler.codec.http.Cookie> decodedCookies = ServerCookieDecoder.decode(cookieString);
            this.cookies = S._for(decodedCookies).map(c -> {
                Cookie ret = new Cookie(c.name(), c.value());
                ret.setComment(c.comment());
                if (S.str.notBlank(c.domain())) ret.setDomain(c.domain());
                ret.setHttpOnly(c.isHttpOnly());
                ret.setMaxAge((int) c.maxAge());
                ret.setSecure(c.isSecure());
                ret.setPath(c.path());
                ret.setVersion(c.version());
                return ret;
            }).val();
        }

        //parse query strings
        params.putAll(S._for(uriDecoder.parameters()).map(list -> list.toArray(new String[list.size()])).val());

        //parse message body (content)
        ByteBuf content = n_req.content();
        if (HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.PATCH.equals(method)) {
            //parse content

            //Multipart
            //TODO
        }

        req.content();

        S._debug(logger, log -> {
            log.debug("parse_time:" + (S.now() - _parse_start));
        });
    }

    @Override
    @Deprecated
    public InputStream in() throws IOException {
        throw new UnsupportedOperationException("netty server doesn't provide input stream");
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

    @Override
    public Locale locale() {
        String sc = S.avoidNull(n_headers.get(HttpHeaderNames.ACCEPT_LANGUAGE), "").toString();
        String[] parsed;
        if (S.str.isBlank(sc)) {
            sc = (String) S.avoidNull(server.env(BaseServer.LOCALE), "zh_CN");
        }
        if ((parsed = sc.split("-")).length >= 2) {
            return new Locale(parsed[0], parsed[1]);
        } else return new Locale("en", "US");
    }

    @Override
    public Map<String, String[]> headers() {
        return headers;
    }

    @Override
    public Map<String, String[]> params() {
        return params;
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
    public Iterable<Cookie> cookies() {
        return cookies;
    }

    @Override
    public String characterEncoding() {
        throw new UnsupportedOperationException("not allowed in netty");
    }
}
