package pond.common.spi;

/**
 * Created by ed on 14-5-20.
 *
 * @see pond.common.JSON
 */
public interface JsonService {

  String toString(Object o);

  <E> E fromString(Class<E> clazz, String s);

}
