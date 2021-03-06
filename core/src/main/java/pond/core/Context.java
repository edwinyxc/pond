package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Tuple;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * The ExecutionContext is continuum of services execution.
 * <ul>
 * <li></li>
 * <li></li>
 * </ul>
 * </p>
 */
public class Context {

  final static Logger logger = LoggerFactory.getLogger(Context.class);
  final static String NAME = "P_NAME";
  final static String ID = "P_ID";
  final static String LATEST = "P_LATEST";

  final private static ThreadLocal<Context> localCtxHolder = new ThreadLocal<>();

  LinkedList<Tuple<String, Object>> content = new LinkedList<>();
  LinkedList<String> err_stack = new LinkedList<>();

  //TODO
  boolean stop = false;
  //interceptors - trigger on each call
  LinkedList<Callback.C2<Context, Service>> interceptors = new LinkedList<>();

  public Context interceptor(Callback.C2<Context, Service> cb) {
    interceptors.add(cb);
    return this;
  }

  public Context(String name) {
    this.set(ID, this.hashCode());
    this.set(NAME, name);
  }

  public void stop() {
    this.stop = true;
  }

  String id() {
    return (String) this.get(ID);
  }

  String user() {
    return (String) this.get(NAME);
  }

  /**
   * pop the last execution result
   *
   * @return
   */
  public Object pop() {
    return this.pop(LATEST);
  }

  public Object peek() {
    return this.get(LATEST);
  }

  public void push(Object ret_val) {
    this.set(LATEST, ret_val);
  }

  public void err(String msg) {
    this.err_stack.addFirst(msg);
  }

  public String err() {
    return this.err_stack.peekFirst();
  }

  /**
   * Get the shared context value and remove it form context
   *
   * @param name
   * @return null if not found or the specified value
   */
  public Object pop(String name) {
    for (Iterator<Tuple<String, Object>> iter = content.iterator(); iter.hasNext(); ) {
      Tuple<String, Object> tuple = iter.next();
      if (name.equals(tuple._a)) {
        iter.remove();
        return tuple._b;
      }
    }
    return null;

  }

  /**
   * Get the shared context value
   *
   * @param name
   * @return null if not found or the specified value
   */
  public Object get(String name) {
    for (Tuple<String, Object> tuple : content) {
      if (name.equals(tuple._a)) {
        return tuple._b;
      }
    }
    return null;
  }


  public List<Object> getAll(String name) {
    return S._for(content).filter(t -> name.equals(t._a)).map(t -> t._b).toList();
  }

  /**
   * Set the shared context value
   *
   * @param name
   */
  public Context set(String name, Object ctx_value) {
    content.addFirst(Tuple.pair(name, ctx_value));
    return this;
  }

  public Context exec(Object... services) {

    localCtxHolder.set(this);
    for (Object serv : services) {

      Service service = Services.adapt(serv);
      S._for(interceptors).each(inter -> inter.apply(this, service));
      if (this.stop) {
        S._debug(logger, log ->
          log.debug("CTX@" + this + "stopped AT SERV@" + serv)
        );
        break;
      }

      //apply service
      service.init(this);
      service.call();
    }
    localCtxHolder.remove();

    return this;
  }

  /**
   * Get Context form the current ThreadLocal
   */
  public static Context current() {
    Context ret = localCtxHolder.get();
    S._assert(ret, "failed to get current context");
    return ret;
  }

  public <E extends Context> E copyContentTo(E newCtx ){
    S._for(this.content).each(entry -> {
      String key = entry._a;
      Object value = entry._b;
      newCtx.set(key, value);
    });
    return newCtx;
  }

  @Override
  public String toString() {
    return "ExecutionContext{" +
        "content=" + content +
        ", err_stack=" + err_stack +
        '}';
  }
}
