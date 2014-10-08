package pond.core.spi;

/**
 * Created by ed on 14-5-20.
 */
public interface JsonService {

    String toString(Object o);

    <E> E fromString(Class<E> clazz, String s);


}
