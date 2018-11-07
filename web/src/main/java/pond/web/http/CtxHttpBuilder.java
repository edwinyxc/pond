package pond.web.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.net.CtxNet;
import pond.net.NetServer;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class CtxHttpBuilder {

    static HttpDataFactory factory;
    static{
        /**
         * For UploadFile
         */
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory

        /**
         * For HttpDataFactory
         */
        factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE); // Disk if size exceed
    }


    /***
    http parser aspect-callbacks
    ***/

    public

    //in-bounds
    final
    List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpRequest>>
        on_HttpRequest = new ArrayList<>(){{
        add((builder, ctx, request) -> {

        });
    }};

    final
    List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, QueryStringDecoder>>
        on_QueryString = new ArrayList<>(){{
        add((builder, ctx, request, decoder) -> {
            //uri-queries
            Map<String, List<String>> parsedParams = decoder.parameters();

        });
    }};
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, HttpContent>> on_HttpContents = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, HttpPostMultipartRequestDecoder>> on_Multipart= new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, FileUpload>> on_Multipart_UploadFile = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, Attribute>> on_Multipart_Attribute = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, LastHttpContent>> on_LastHttpContent= new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, WebSocketFrame>> on_WebSocketFrames = new ArrayList<>();

    //out-bounds

    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpResponse>> on_SendResponse = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpResponse, ByteBuf>> on_SendNormal = new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpResponse>> on_SendUnhandled = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpResponse, RandomAccessFile>> on_SendFile = new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, Throwable >> on_ExceptionCaught = new ArrayList<>();


    public CtxHttpBuilder onHttpRequest(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpRequest> handler){
        on_HttpRequest.add(handler);
        return this;
    }
    public CtxHttpBuilder onHttpContent(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpContent> handler){
        on_HttpContents.add(handler);
        return this;
    }

    public CtxHttpBuilder onLastHttpRequest(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, LastHttpContent> handler){
        on_LastHttpContent.add(handler);
        return this;
    }

    public CtxHttpBuilder onWebSocketFrames(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, WebSocketFrame> handler){
        on_WebSocketFrames.add(handler);
        return this;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    private void sendBadRequest(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_REQUEST))
            .addListener(ChannelFutureListener.CLOSE);
    }

    public SimpleChannelInboundHandler<Object> build(){
        return new SimpleChannelInboundHandler<Object>() {

            HttpPostRequestDecoder postRequestDecoder;

            private void receiveHttpRequest(ChannelHandlerContext ctx, HttpRequest request){

                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                boolean is_keepAlive = HttpUtil.isKeepAlive(request);
                boolean is_multipart = HttpPostRequestDecoder.isMultipart(request);



                S._debug(NetServer.logger, log -> {
                    log.debug("GOT HTTP REQ: ");
                    log.debug(request.toString());
                });

                if(!request.decoderResult().isSuccess()){
                    sendBadRequest(ctx);
                }

                if(is_multipart) {
                    HttpMethod method = request.method();
                    if(S._in(method, HttpMethod.PATCH, HttpMethod.PUT, HttpMethod.POST)){
                        try {
                            postRequestDecoder = new HttpPostRequestDecoder(factory, request);
                        } catch (Throwable e) {
                            NetServer.logger.error(e.getMessage(), e);
                            sendBadRequest(ctx);
                        }
                    }
                }
                PooledByteBufAllocator.DEFAULT.


            }

            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {


            }


        };
    }

}
