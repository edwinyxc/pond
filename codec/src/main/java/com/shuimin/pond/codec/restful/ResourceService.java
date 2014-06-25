package com.shuimin.pond.codec.restful;

import com.shuimin.common.f.Tuple;
import com.shuimin.common.sql.Criterion;
import com.shuimin.common.sql.Sql;
import com.shuimin.common.sql.SqlSelect;
import com.shuimin.pond.core.Renderable;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.db.DB;
import com.shuimin.pond.core.db.Page;
import com.shuimin.pond.core.db.Record;
import com.shuimin.pond.core.exception.HttpException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.shuimin.common.S._for;
import static com.shuimin.common.f.Tuple.t2;

/**
 * Created by ed on 14-5-20.
 */
public abstract class ResourceService<E extends Record> {

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
        String pkLbl = r.primaryKeyName();
        SqlSelect select =
                Sql.select().from(tableName).where(
                        pkLbl, Criterion.EQ, id);
        List<E> l = DB.fire(tmpl ->
                tmpl.map(r.mapper()::map, select.tuple()));
        return _for(l).first();
    }

    public SqlSelect selectSql(Request req) {
        E r = getProto();
        String tb_name = r.table();
        Set<String> fields = r.declaredFields();
        return Sql.select(fields.toArray(new String[fields.size()])).from(tb_name)
                .where(req.getQuery(r, req));
    }

    public Tuple<List<E>,Integer> queryForList(SqlSelect sql){
        E r = getProto();
        return DB.fire(tmpl -> {
            List<E> result =
            tmpl.map(r.mapper()::map,sql.tuple());
            int count = tmpl.count(sql.count().tuple());
            return t2(result,count);
        });
    }

    @SuppressWarnings("unchecked")
    public Page queryForPage(Request req) {
        E r = getProto();

        return DB.fire(tmpl -> {
            Page page = Page.of(req);
            SqlSelect select = selectSql(req);
            if (Page.allowPage(req))
                select.offset(Page.getOffset(req))
                        .limit(Page.getLimit(req));
            List<E> data =
                    tmpl.map(r.mapper()::map, select.tuple());
            int count = tmpl.count(select.count().tuple());
            List<Map<String,Object>> view =
                    _for(data).map(Record::view).toList();
            return page.fulfill(view , count);
        });

    }


    public Renderable render(String accept, Object o) {
        if (accept.startsWith("application/json")
                || accept.startsWith("text/json")) {
            return Renderable.json(o);
        }
        return Renderable.json(o);
    }


    /**
     * Delete a record, returns its id, if success
     *
     * @param id id
     * @return id
     */
    public String delete(String id) {
        Record r = get(id);
        if (r == null) throw new HttpException(404, "Record[" + id + "] not found.");
        if (DB.fire(tmpl -> tmpl.del(r)))
            return id;
        throw new HttpException(500, "Delete record " + id + " not success.");
    }

    public Record create(Request request) {
        @SuppressWarnings("unchecked")
        E a = (E) Record.newEntity(prototype().getClass())
                .of(request);
        if (DB.fire(t -> t.add(a)))
            return a;
        throw new HttpException(500, "Create record not success.");
    }

    public Record update(String id, Request request) {
        Record e = get(id);
        if (e == null) throw new HttpException(404, "Record[" + id + "] not found.");
        e.of(request);
        if (DB.fire(tmpl -> tmpl.upd(e)))
            return e;
        throw new HttpException(500, "Update record " + id + " not success.");
    }
}
