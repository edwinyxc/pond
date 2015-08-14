package pond.core;

/**
 * Created by ed on 2014/4/18.
 */
public interface Session {

  String id();

  Object get(String key);

  Session set(String key, Object value);

}
