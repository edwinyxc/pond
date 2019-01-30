package pond.web.restful;

import pond.common.S;
import pond.db.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by edwin on 3/12/2017.
 */
public class Schema extends HashMap<String, Object> {

    static Map<Class<?>, Schema> innerCache = new ConcurrentHashMap<>();

    private Schema() {
    }

    public Schema required(String... names) {
        this.put("required", names);
        return this;
    }

//    public Schema xml()

    public static Schema STRING() {
        return S._tap(new Schema(), s -> s.put("type", "string"));
    }

    public static Schema ARRAY(Map<String, Object> itemSchema) {
        return S._tap(new Schema(), s -> {
            s.put("type", "array");
            s.put("items", itemSchema);
        });
    }

    public static Schema FILE() {
        return S._tap(new Schema(), s -> s.put("type", "file"));
    }

//    static private Map<String, Object> proto(Record record) {
//        S._for(record.declaredFields()).each(f -> {
//        });
//    }

    static private Map<String, Object> proto(Map<String, Object> proto) {
        return S._for(proto).map(o -> {
            Object ret;
            if (o instanceof Map) {
                ret = proto((Map<String, Object>) o);
            } else {
                ret = new HashMap<String, String>() {{
                    //TODO
                    put("type", "string");
                }};
            }
            return ret;
        }).val();
    }

    static private Map<String, Object> paramSchema(Map<String, ParamDef> proto) {
        return S._for(proto).map(o -> {
            Object ret;
            if (o instanceof ParamDefStruct) {
                ret = paramSchema((Map<String, ParamDef>) o);
            } else {
                ret = new HashMap<String, String>() {{
                    put("type", o.type.val);
                }};
            }
            return ret;
        }).val();
    }

    public static Schema OBJECT(Map<String, Object> proto) {
        return S._tap(new Schema(), s -> {
            s.put("type", "object");
            s.put("properties", proto(proto));
        });
    }

//    public static Schema OBJECT_MODEL(DB db, Class<? extends Record> type) {
//        Record proto = Prototype.proto(type);
//        String tableName = proto.table();
//        Map<String, Map<String, Integer>> dbStruct = db.getDbStructures();
//        Map<String, Integer> tableStruct = dbStruct.getEntry(tableName);
//        S._assert(tableStruct != null, "No such table found : " + tableName);
//        return S._tap(new Schema(), s -> {
//            s.put("type", "object");
//            s.put("properties", S._tap(new HashMap(), m -> {
//                S._for(proto.declaredFields()).each(f -> {
//                    int type_int = tableStruct.getEntry(f.name());
//                    //insert new schema entry
//                    s.put(f.name(), )
//                });
//            }));
//        });
//    }


    public static Schema PARAMS_SCHEMA(Map<String, ParamDef> proto) {
        return S._tap(new Schema(), s -> {
            s.put("type", "object");
            s.put("properties", paramSchema(proto));
        });
    }

}


