package pond.core.spi.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import pond.common.S;
import pond.core.http.AbstractRequest;
import pond.core.http.Cookie;

import java.net.InetSocketAddress;
import java.util.Map;

public class NettyReqWrapper extends AbstractRequest {

    HttpRequest n_req;

    final Channel channel;


    public NettyReqWrapper(ChannelHandlerContext ctx, HttpRequest req ) {
        channel = ctx.channel();
        n_req = req;
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
        return n_req.uri();
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
    public String method() {
        return n_req.method().toString();
    }

    public String realIp(){
        HttpHeaders n_headers = n_req.headers();
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
    public String remoteIp() {
        return  channel.remoteAddress().toString();
    }

    @Override
    public Map<String, Cookie> cookies() {
        return cookies;
    }

    @Override
    public String toString() {
        StringBuilder dump = new StringBuilder(super.toString());
        dump.append("\n");
        dump.append("HEADERS ").append(S.dump(headers)).append("\n");
        dump.append("PARAMETERS ").append(S.dump(params)).append("\n");
        dump.append("FILES ").append(S.dump(uploadFiles)).append("\n");
        dump.append("COOKIES ").append(S.dump(cookies)).append("\n");
        dump.append("\n");
        return dump.toString();
    }
}
