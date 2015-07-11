package pond.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 10/31/14.
 */
class Proto {
    static Map<Class<? extends Record>, Record> protos = new HashMap<>();

    static Map<Class,RecordService> daos = new HashMap();

    static <E extends Record> E proto(Class<E> cls) {
        Record t = protos.get(cls);

        if( t == null ) {
            t = Record.newValue(cls);
            protos.put(cls, t);
        }

        return (E) t;
    }

    static <E extends Record> RecordService<E> dao(Class<E> cls) {
        RecordService<E> eServ = (RecordService<E>) Proto.daos.get(cls);
        if( eServ == null ){
            eServ = RecordService.build( cls );
            Proto.daos.put(cls,eServ);
        }
        return eServ;
    }




}
