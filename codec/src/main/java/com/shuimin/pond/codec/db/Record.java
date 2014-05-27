package com.shuimin.pond.codec.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Holder;
import com.shuimin.pond.core.Request;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ed on 2014/4/24.
 */
public interface Record extends Map<String, Object> {

    static Holder<Function<String, Class>> tableNameSupplierHolder =
            //default as class-name-under-score
            new Holder<Function<String, Class>>().init(clazz ->
                    S.str.underscore(clazz.getSimpleName()));

    static String tableName(Class<? extends Record> recordClass) {
        return tableNameSupplierHolder.val.apply(recordClass);
    }

    static void setTableNameStrategy(Function<String, Class> func) {
        if (func == null) throw new NullPointerException("function can not be null");
        tableNameSupplierHolder.val = func;
    }

    final static String DEFAULT_PRI_KEY = "id";

    static Holder<Function.F0<Object>> PK_PROVIDER =
            new Holder<Function.F0<Object>>().init(S.uuid::vid);

    static void setPriKeyProvider(Function.F0<Object> provider) {
        PK_PROVIDER.val = provider;
    }

    default public Set<String> fields() {
        return this.keySet();
    }

    public String PK();

    public String PKLabel();

    public Record PK(Object pk);

    public String table();

    public Record table(String s);

    <E> E get(String s);

    Record set(String s, Object val);

    void PKLabel(String label);

    @SuppressWarnings("unchecked")
    Record merge(Request req);

    Record merge(Map map);

    <E> RowMapper<E> mapper();

    <E> Record mapper(RowMapper<E> mapper);

    List<Record> innerRecords();

    default void save() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.add(this));
    }

    default void update() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.upd(this));
    }

    default void delete() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.del(this));
    }

    Record add(Record r);

    Record setInner(String tableName, String colName, Object val);

    Record getInnerRecord(String tableName);

    static <T> T newEntity(Class<T> recordClass) {
        Record t = (Record) newValue(recordClass);
        t.PK(PK_PROVIDER.val.apply());
        return (T) t;
    }

    static <T> T newValue(Class<T> recordClass) {
        try {
            return S._one(recordClass);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
