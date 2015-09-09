package pond.web.spi.netty;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.CharsetUtil;
import pond.web.Response;

import java.io.OutputStream;
import java.io.RandomAccessFile;

public class HandlerExecutionContext {

  public final static int UNHANDLED = 0;
  public final static int NORMAL = 1;
  public final static int STATIC_FILE = 2;
  public final static int ERROR = -1;

  RandomAccessFile sendfile;
  Long sendfile_offset;
  Long sendfile_length;
  int type = UNHANDLED;

  OutputStream out;

  Response resp;

  Throwable cause;

  HandlerExecutionContext(){}


  public HandlerExecutionContext normal(OutputStream out) {
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

  public HandlerExecutionContext file(RandomAccessFile file, Long offset, Long length) {
    sendfile = file;
    sendfile_offset = offset;
    sendfile_length = length;
    this.type = STATIC_FILE;
    return this;
  }

  public boolean isSuccess() {
    return type != ERROR;
  }

  public Response response() {
    return resp;
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder(super.toString());
    ret.append("TYPE: ").append(type).append("\n");
    ret.append("OUT: ").append(((ByteBufOutputStream) out).buffer().toString(CharsetUtil.UTF_8)).append("\n");
    return ret.toString();
  }
}
