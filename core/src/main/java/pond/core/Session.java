package pond.core;

/**
 * Created by ed on 2014/4/18.
 */
public interface Session {

    public String id();

    public Object get(String key);

    public Session set(String key, Object value);

}
