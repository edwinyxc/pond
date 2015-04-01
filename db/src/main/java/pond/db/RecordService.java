package pond.db;

import pond.common.f.Tuple;
import pond.common.sql.Criterion;
import pond.common.sql.Sql;
import pond.common.sql.SqlSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pond.common.S._for;
import static pond.common.f.Tuple.t2;

/**
 * Created by ed on 14-5-20.
 */
public abstract class RecordService<E extends Record> {

    public static <E extends Record> RecordService<E> build(Class<E> clazz) {
        return build(Proto.proto(clazz));
    }

    public static <E extends Record> RecordService<E> build(E p) {
        return new RecordService<E>() {
            @Override
            E prototype() {
                return p;
            }
        };
    }

    private E _proto;

    abstract E prototype();

    protected E getProto() {
        if (_proto == null)
            _proto = prototype();
        return _proto;
    }

    @SuppressWarnings("unchecked")
    public E get(String id) {
        Record r = getProto();
        String tableName = r.table();
        String pkLbl = r.idName();
        SqlSelect select =
                Sql.select().from(tableName).where(
                        pkLbl, Criterion.EQ, id);
        List<E> l = r._getDB().get(tmpl ->
                tmpl.query(r.mapper(), select.tuple()));
        return _for(l).first();
    }

    /**
     * <pre>
     * Query record(s) on specified arguments
     * if there is no arguments, return Select all;
     *
     * if only one argument, the argument will treated as raw sql
     *  i.e. queryRS("id = '123' AND something LIKE '%else%' ")
     *      => Select ... from ... where id = '123' AND something LIKE '%else%';
     *
     * if arguments has a 3-multiple length and each 3 has a form of
     * (String,Criterion,String)  or (String,Criterion,String[])
     *  i.e.
     *  queryRS("id", Criterion.EQ, "13")
     *      => Select ... from  ... where id = '13'
     *  queryRS("id",Criterion.IN, {"1","2","3","4"})
     *      => Select ... from ... where id IN ("1","2","3","4");
     * </pre>
     *
     * @param args
     * @return
     */
    public List<E> query(Object... args) {
        E r = getProto();
        SqlSelect sqlSelect;
        Set<String> d_fields = r.declaredFieldNames();
        String[] fields = new String[d_fields.size()];
        fields = d_fields.toArray(fields);
        sqlSelect = Sql.select(fields).from(r.table());
        if (args.length == 0) {
            //do nothing for query all
        } else if (args.length == 1) {
            sqlSelect.where((String) args[0]);
        } else if (args.length > 2) {
            //1. split args by criterion
            List<List<Object>> arg_groups = new ArrayList<>();
            List<Object> group = new ArrayList<>();
            Object cur;
            Object last;
            for (int i = 0; i < args.length; i++) {
                cur = args[i];
                last = (i - 1) >= 0 ? args[i - 1] : null;
                if (last != null && cur instanceof Criterion) {
                    //size > 2 is the valid size
                    if (group.size() > 2) {
                        //put queryRS group into groups
                        //[key,cri,args...,(key),(cri)]
                        //remove the redundant key
                        group.remove(group.size() -1);
                        arg_groups.add(group);
                    }
                    //new an array for put
                    group = new ArrayList<>();
                    //add key
                    group.add(last);
                    //add criterion
                    group.add(cur);
                } else {
                    group.add(cur);
                }
            }
            arg_groups.add(group);
            //2.make queryRS
            for (List q_group : arg_groups) {
                if (q_group.size() > 2) {
                    sqlSelect.where((String) q_group.remove(0),
                            (Criterion) q_group.remove(0),
                            (String[]) q_group.toArray(new String[q_group.size()]));
                }
                //else ignore illegal arguments
            }
        } else {
            throw new RuntimeException(
                    "argument length should be >3 or 1 or 0"
            );
        }
//        System.out.println(sqlSelect.debug());
        return r._getDB().get(tmpl ->
                tmpl.query(r.mapper(), sqlSelect.tuple()));
    }


    public Tuple<List<E>, Integer> list(SqlSelect sql) {
        E r = getProto();
        return r._getDB().get(tmpl -> {
            List<E> result =
                    tmpl.query(r.mapper(), sql.tuple());
            int count = tmpl.count(sql.count().tuple());
            return t2(result, count);
        });
    }


    /**
     * Delete a record, returns its id, if success
     *
     * @param id id
     * @return id
     */
    @SuppressWarnings("unchecked")
    public String delete(String id) {
        Record r = get(id);
        if (r != null)
            r._getDB().post(tmpl -> tmpl.del(r));
        return id;
    }

    public void delete(Record record) {
        record.delete();
    }

    /**
     * Add record to db.
     * @param record
     */
    public void add(Record record) {
        record.add();
    }

    @SuppressWarnings("unchecked")
    public Record create(Map<String, Object> p) {
        E a = (E) Record.newEntity(prototype().getClass())
                .merge(p);
        a._getDB().post(t -> t.add(a));
        return a;
    }

    public void update(Record record) {
        record.update();
    }

    @SuppressWarnings("unchecked")
    public Record update(String id, Map<String, Object> request) {
        Record e = get(id);
        if (e != null) {
            e.merge(request);
            e._getDB().post(tmpl -> tmpl.upd(e));
        }
        return e;
    }

}
