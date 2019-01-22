package pond.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class STREAM {

  private static final int BUFFER_SIZE = 8192;

  public static String readFully(InputStream inputStream, Charset encoding)
      throws IOException {
    return new String(readFully(inputStream), encoding);
  }

  public static String readFully(InputStream inputStream, String encoding)
      throws IOException {
    return new String(readFully(inputStream), encoding);
  }

  public static byte[] readFully(InputStream inputStream)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }


  public static void write(InputStream in, OutputStream out) throws IOException {
    pipe(in, out);
  }

  /**
   * pipe from is to os;
   *
   * @param in  inputStream
   * @param out outputStream
   * @throws java.io.IOException
   */
  public static void pipe(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int cnt;

    while ((cnt = in.read(buffer)) != -1) {
      out.write(buffer, 0, cnt);
    }
  }
}
