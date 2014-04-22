package com.shuimin.pond.core.server.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.servlet.ServletInputStream;
import java.io.IOException;

public class NettyInputStream extends ServletInputStream {

    private final FullHttpRequest request;

    private final ByteBufInputStream in;

    public NettyInputStream(FullHttpRequest request) {
        this.request = request;
        this.in = new ByteBufInputStream(request.content());
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return this.in.read(buf);
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {
        return this.in.read(buf, offset, len);
    }

}
