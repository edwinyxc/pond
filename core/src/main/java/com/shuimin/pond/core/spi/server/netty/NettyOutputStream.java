
package com.shuimin.pond.core.spi.server.netty;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.FullHttpResponse;

import javax.servlet.ServletOutputStream;
import java.io.IOException;


public class NettyOutputStream extends ServletOutputStream {

    private final FullHttpResponse response;

    private final ByteBufOutputStream out;

    private boolean flushed = false;

    public NettyOutputStream(FullHttpResponse response
    ) {
        this.response = response;
        this.out = new ByteBufOutputStream(response.content());
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void write(byte[] b, int offset, int len) throws IOException {
        this.out.write(b, offset, len);
    }

    @Override
    public void flush() throws IOException {
        this.flushed = true;
    }

    public void resetBuffer() {
        this.out.buffer().clear();
    }

    public boolean isFlushed() {
        return flushed;
    }

    public int getBufferSize() {
        return this.out.buffer().capacity();
    }
}
