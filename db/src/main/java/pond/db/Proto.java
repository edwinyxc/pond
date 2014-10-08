package pond.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 9/29/14.
 */
public class Proto extends AbstractRecord{

    static Map<Class<?>, Record> protos = new HashMap<>();

    static <E extends Record> E proto(Class<E> eClass) {
        @SuppressWarnings("unchecked")
        E e = (E) protos.get(eClass);
        if( e == null ) {
            //All protos are values
            e = Record.newValue(eClass);
            protos.put(eClass,e);
        }
        return e;
    }


}
