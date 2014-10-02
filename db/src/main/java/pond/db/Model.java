package pond.db;

import static pond.common.S._try;

/**
 * Created by ed on 9/29/14.
 */
public class Model extends AbstractRecord {

    public static <E extends Model> RecordService<E> dao(){
        RecordService<E> service =
                _try(() ->
                        RecordService.build((Class<E>) _class()));
        return service;
    }

    private static <T extends Model> Class<T> _class() throws ClassNotFoundException {
        return (Class<T>) Class.forName(getClassName());
    }


    private static String getClassName() {
        return new ClassGetter().getClassName();
    }

    static class ClassGetter extends SecurityManager {
        public String getClassName() {
            Class[] classes = getClassContext();
            for (Class clazz : classes) {
                if (Model.class.isAssignableFrom(clazz) && !clazz.equals(Model.class)) {
                    return clazz.getName();
                }
            }
            throw new RuntimeException(
                    "failed to determine Model class name, are you sure models have been instrumented?");
        }
    }

}
