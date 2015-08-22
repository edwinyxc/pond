package pond.web;

import pond.web.http.Cookie;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * <p>Response 封装了http response 对象，
 * 所有jtiny支持的服务器都应该提供相应适配器来将底层对象转换成Response</p>
 */
public interface Response {

  /**
   * <p>写入响应头</p>
   *
   * @param k key
   * @param v value
   * @return this
   */
  Response header(String k, String v);

  /**
   * <p>向客户端发送对应代码,内容一并送出
   * 此方法调用之后任何对其的操作都被视为无效，具体细节参考服务期实现。</p>
   *
   * @param code http status code
   */
  default void send(int code) {
    send(code, "");
  }

  /**
   * <p>Send message to client with code 200</p>
   *
   * @param msg
   */
  default void send(String msg) {
    send(200, msg);
  }

  /**
   * <p>此方法用来发送错误码和详细描述</p>
   *
   * @param code
   * @param msg
   */
  void sendError(int code, String msg);

  /**
   * @param code error code
   * @param msg  error message
   */
  void send(int code, String msg);

  /**
   * <p>向客户端写入文件，完成时发送200 或者　206 (range)，此操作立即返回，具体如何发送由底层服务器控制。</p>
   * big file
   *
   * @param file attachment
   */
  void sendFile(File file, long offset, long length);

  default void sendFile(File file) {
    sendFile(file, 0, file.length());
  }

  /**
   * <p>设置响应状态码，只要不发送，此状态码可以再次改变</p>
   *
   * @param sc status code
   * @return this
   */
  Response status(int sc);

  /**
   * <p>获取底层响应流，原因是具体的底层响应可能不是以流的方式实现的。</p>
   *
   * @return outputStream
   */
  OutputStream out();

  /**
   * <p>包装了out流的printWriter,用于输出响应文本</p>
   *
   * @return printWriter
   */
  PrintWriter writer();

  /**
   * <p>向响应添加字符串</p>
   *
   * @param s to append string
   * @return this
   */
  Response write(String s);

  /**
   * <p>为响应添加cookie, 使用 Add-Cookie 响应头实现</p>
   *
   * @param c
   * @return this
   */
  Response cookie(Cookie c);

  /**
   * <p>发送302响应，参数作为Location</p>
   *
   * @param url destination location
   */
  void redirect(String url);

  /**
   * <p>设置 Content-Type 响应头
   * </p>
   *
   * @param type type
   * @return this
   */
  Response contentType(String type);

  default void render(Render r) {
    r.render(ctx().req, ctx().resp);
  }

  default Ctx ctx() {
    return CtxExec.get();
  }


}
