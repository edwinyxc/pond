package pond.db;

import static pond.common.S._try;
import java.util.Map;
import java.util.HashMap;
/**
 * Created by ed on 9/29/14.
 */
public class Model extends AbstractRecord {
        
    private static Map<Class,RecordService> daos = new HashMap();

    public static <E extends Model> RecordService<E> dao(Class cls){
        RecordService<E> eServ = (RecordService<E>) daos.get(cls);
        if( eServ == null ){
            eServ = RecordService.build( cls );
            daos.put(cls,eServ);
        }
        return eServ;
    }
}
