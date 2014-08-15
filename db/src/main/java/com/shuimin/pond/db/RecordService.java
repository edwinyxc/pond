package com.shuimin.pond.db;

import com.shuimin.common.f.Function;
import com.shuimin.common.f.Tuple;
import com.shuimin.common.sql.Criterion;
import com.shuimin.common.sql.Sql;
import com.shuimin.common.sql.SqlSelect;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.shuimin.common.S._for;
import static com.shuimin.common.f.Tuple.t2;

/**
 * Created by ed on 14-5-20.
 */
public abstract class RecordService<E extends Record> {

    public static <E extends Record> RecordService<E> build(Function.F0<E> p) {
        return new RecordService<E>() {
            @Override
            E prototype() {
                return p.apply();
            }
        };
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
        List<E> l = DB.fire(tmpl ->
                tmpl.map(r.mapper()::map, select.tuple()));
        return _for(l).first();
    }

    /**
     * <pre>
     * Query record(s) on specified arguments
     * if there is no arguments, return Select all;
     *
     * if only one argument, the argument will treated as raw sql
     *  i.e. query("id = '123' AND something LIKE '%else%' ")
     *      => Select ... from ... where id = '123' AND something LIKE '%else%';
     *
     * if arguments has a 3-multiple length and each 3 has a form of
     * (String,Criterion,String)  or (String,Criterion,String[])
     *  i.e.
     *  query("id", Criterion.EQ, "13")
     *      => Select ... from  ... where id = '13'
     *  query("id",Criterion.IN, {"1","2","3","4"})
     *      => Select ... from ... where id IN ("1","2","3","4");
     *</pre>
     * @param args
     * @return
     */
    public List<E> query(Object... args) {
        E r = getProto();
        SqlSelect sqlSelect;
        Set<String> d_fields = r.declaredFields();
        String[] fields = new String[d_fields.size()];
        fields = d_fields.toArray(fields);
        sqlSelect = Sql.select(fields).from(r.table());
        if (args.length == 0) {
        } else if (args.length == 1) {
            sqlSelect.where((String) args[0]);
        } else if (args.length >= 3 && args.length % 3 == 0) {
            for (int i = 0; i < args.length; i += 3) {
                Object key = args[i];
                Object criterion = args[i + 1];
                Object value = args[i + 2];
                if (key instanceof String
                        && criterion instanceof Criterion) {
                    if (value instanceof String[])
                        sqlSelect.where((String) key, (Criterion) criterion,
                                (String[]) value);
                    else if (value instanceof String)
                        sqlSelect.where((String) key, (Criterion) criterion,
                                (String) value);
                } else {
                    throw new RuntimeException(
                            "argument type not fulfilled," +
                                    "please use String,Criterion,String[] as a couple");
                }
            }
        } else {
            throw new RuntimeException(
                    "argument length should be n*3 or 1 or 0"
            );
        }
//        System.out.println(sqlSelect.debug());
        RowMapper<E> rm = r.mapper();
        return DB.fire(tmpl ->
                tmpl.map(rm::map, sqlSelect.tuple()));
    }


    public Tuple<List<E>, Integer> list(SqlSelect sql) {
        E r = getProto();
        return DB.fire(tmpl -> {
            List<E> result =
                    tmpl.map(r.mapper()::map, sql.tuple());
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
    public String delete(String id) {
        Record r = get(id);
        if (r != null)
            DB.fire(tmpl -> tmpl.del(r));
        return id;
    }

    public String delete(Record record) {
        DB.fire(tmpl -> tmpl.del(record));
        return record.id();
    }

    public void create(Record record) {
        DB.fire(t -> t.add(record));
    }

    public Record create(Map<String, Object> p) {
        @SuppressWarnings("unchecked")
        E a = (E) Record.newEntity(prototype().getClass())
                .merge(p);
        DB.fire(t -> t.add(a));
        return a;
    }

    public void update(Record record) {
        record.update();
    }

    public Record update(String id, Map<String, Object> request) {
        Record e = get(id);
        if (e != null) {
            e.merge(request);
            DB.fire(tmpl -> tmpl.upd(e));
        }
        return e;
    }

}
