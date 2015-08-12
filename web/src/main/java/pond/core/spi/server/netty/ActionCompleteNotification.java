package pond.core.spi.server.netty;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.CharsetUtil;
import pond.core.Request;
import pond.core.Response;

import java.io.OutputStream;
import java.io.RandomAccessFile;

public class ActionCompleteNotification {
    public final static int UNHANDLED = 0;
    public final static int NORMAL = 1;
    public final static int STATIC_FILE = 2;
    public final static int ERROR = -1;

    RandomAccessFile sendfile;
    Long sendfile_offset;
    Long sendfile_length;
    int type = UNHANDLED;

    OutputStream out;

    final Request req;
    final Response resp;


    Throwable cause;

    public ActionCompleteNotification(Request req, Response resp) {
        this.req = req;
        this.resp = resp;
    }

    public ActionCompleteNotification normal(OutputStream out) {
        this.type = NORMAL;
        this.out = out;
        return this;
    }

    public int type() {
        return type;
    }

    public RandomAccessFile sendfile() {
        return sendfile;
    }

    public Long sendFileLength() {
        return sendfile_length;
    }

    public Long sendFileOffset() {
        return sendfile_offset;
    }

    public OutputStream out() {
        return out;
    }

    public ActionCompleteNotification file(RandomAccessFile file, Long offset, Long length) {
        sendfile = file;
        sendfile_offset = offset;
        sendfile_length = length;
        this.type = STATIC_FILE;
        return this;
    }

    public boolean isSuccess() {
        return type != ERROR;
    }

    public Request request() {
        return req;
    }

    public Response response() {
        return resp;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(super.toString());
        ret.append("TYPE: ").append(type).append("\n");
        ret.append("OUT: ").append(((ByteBufOutputStream) out).buffer().toString(CharsetUtil.UTF_8)).append("\n");
        return ret.toString();
    }
}
