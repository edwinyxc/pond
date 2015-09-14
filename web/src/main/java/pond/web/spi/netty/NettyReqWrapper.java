package pond.web.spi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import pond.common.S;
import pond.web.AbstractRequest;
import pond.web.http.Cookie;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class NettyReqWrapper extends AbstractRequest {

  HttpRequest n_req;

  final Channel channel;

  InputStream _in;

  public NettyReqWrapper(
      ChannelHandlerContext cctx,
      HttpRequest req) {
    channel = cctx.channel();
    n_req = req;
  }

  public void content(ByteBuf content) {
    S._assert(content);
    _in = new ByteBufInputStream(content);
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


  @Override
  public String remoteIp() {
    return channel.remoteAddress().toString();
  }

  @Override
  public InputStream in() {
    return _in;
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
