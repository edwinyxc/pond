package com.shuimin.pond.core.spi.server.netty;

import com.shuimin.common.S;
import com.shuimin.pond.core.http.AbstractRequest;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author ed
 */
public class NettyRequest extends AbstractRequest {

    final Channel channel;
    final InputStream in;
    final private FullHttpRequest httpRequest;
    final private List<Cookie> cookies;
    final private QueryStringDecoder queryStringDecoder;
    final private Map<String, String[]> parameterMap;
    private Map<String, String[]> headers = new HashMap<>();

    public NettyRequest(FullHttpRequest httpRequest, Channel channel) {
        this.httpRequest = httpRequest;
        this.channel = channel;
        this.queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
        String cookie_str
                = httpRequest.headers().get(HttpHeaders.Names.COOKIE);
        cookies = S.list.one(S._for(CookieDecoder
                .decode(cookie_str != null ? cookie_str : "")).<Cookie>map((cookie) -> {
            Cookie sc = new Cookie(cookie.getName(), cookie.getValue());
            if (S.str.notBlank(cookie.getDomain())) {
                sc.setDomain(cookie.getDomain());
            }
            sc.setMaxAge((int) cookie.getMaxAge());
            sc.setHttpOnly(cookie.isHttpOnly());
            sc.setPath(cookie.getPath());
            sc.setSecure(cookie.isSecure());
            sc.setVersion(cookie.getVersion());
            sc.setComment(cookie.getComment());
            return sc;
        }).val());

        S._for(httpRequest.headers().names()).each((name) -> {
            String[] head = S._for(httpRequest.headers().getAll(name)).join();
            headers.put(name, head);
        });

        parameterMap
                = S._for(queryStringDecoder.parameters()).<String[]>map(
                (list) -> (S.array.of(list))).val();

        in = new NettyInputStream(httpRequest);

    }

    public QueryStringDecoder queryStringDecoder() {
        return queryStringDecoder;
    }

    @Override
    public InputStream in() throws IOException {
        return in;
    }

    @Override
    public String uri() {
        return fullUri();
    }

    private String fullUri() {
        String protocol = HttpVersion.HTTP_1_1.protocolName();

        String hostName = ((InetSocketAddress) channel.localAddress()).getHostName();

        //Fuck IPv6 according javaDoc
        // ipv6 string should contained between '[]'
        // fuck java fuck me
        if (hostName.contains(":")) { /*an ipv6*/
            hostName = "[" + hostName + "]";
        }

        String port = String.valueOf(
                ((InetSocketAddress) channel.localAddress()).getPort());
        return protocol + "://" + hostName + ":" + port + httpRequest.getUri();
    }

    @Override
    public Locale locale() {
        String sc = httpRequest
                .headers().get(HttpHeaders.Names.ACCEPT_LANGUAGE);
        String[] parsed;
        if (S.str.notBlank(sc) && (parsed = sc.split("-")).length == 2) {
            return new Locale(parsed[0], parsed[1]);
        }
        return new Locale("en", "US");//XXX change to zh_CN
    }

    @Override
    public Map<String, String[]> headers() {
        return headers;
    }

    @Override
    public Map<String, String[]> params() {
        return parameterMap;
    }

    @Override
    public String method() {
        return httpRequest.getMethod().name();
    }

    @Override
    public String remoteIp() {
        return ((InetSocketAddress) channel.remoteAddress())
                .getAddress().getHostAddress();
    }

    @Override
    public Iterable<Cookie> cookies() {
        return cookies;
    }

    @Override
    public String characterEncoding() {
        //FIXME:
        return null;
    }

}
