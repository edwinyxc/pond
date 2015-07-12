package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.FullHttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;


public class NettyOutputServletStream extends ServletOutputStream {

    private final ByteBufOutputStream out;

    public NettyOutputServletStream(ByteBuf buffer
    ) {
        this.out = new ByteBufOutputStream(buffer);
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
        out.flush();
    }

    public void resetBuffer() {
        this.out.buffer().clear();
    }

    public int getBufferSize() {
        return this.out.buffer().capacity();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void close() throws IOException {
        out.close();
        super.close();
    }
}
