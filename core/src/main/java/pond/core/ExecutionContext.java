package pond.core;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.*;

/**
 * <p>
 *   The ExecutionContext is continuum of services execution.
 *   <ul>
 *     <li></li>
 *     <li></li>
 *   </ul>
 * </p>
 */
public class ExecutionContext {

  final static String USER   = "P_OWNER";
  final static String ID     = "P_ID";
  final static String LATEST = "P_LATEST";

  LinkedList<Tuple<String,Object>> content = new LinkedList<>();
  LinkedList<String> err_stack = new LinkedList<>();

  boolean stop = false;

  public ExecutionContext(String user){
    this.set(ID, this.hashCode());
    this.set(USER, user);
  }

  public void stop(){
    this.stop = true;
  }

  String id() {
    return (String) this.get(ID);
  }

  String user() {
    return (String) this.get(USER);
  }

  /**
   * pop the last execution result
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
   * @param name
   * @return null if not found or the specified value
   */
  public Object pop(String name) {
    for(Iterator<Tuple<String, Object>> iter = content.iterator(); iter.hasNext();) {
      Tuple<String, Object> tuple =  iter.next();
      if(name.equals(tuple._a)){
        iter.remove();
        return tuple._b;
      }
    }
    return null;

  }

  /**
   * Get the shared context value
   * @param name
   * @return null if not found or the specified value
   */
  public Object get(String name) {
    for(Tuple<String,Object> tuple : content) {
      if(name.equals(tuple._a)){
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
   * @param name
   */
  public void set(String name, Object ctx_value) {
    content.addFirst(Tuple.pair(name, ctx_value));
  }

  @Override
  public String toString() {
    return "ExecutionContext{" +
        "content=" + content +
        ", err_stack=" + err_stack +
        '}';
  }
}
