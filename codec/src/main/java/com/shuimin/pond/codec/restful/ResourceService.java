package com.shuimin.pond.codec.restful;

import com.shuimin.pond.core.db.DB;
import com.shuimin.pond.core.db.Page;
import com.shuimin.pond.core.db.Record;
import com.shuimin.common.sql.Criterion;
import com.shuimin.common.sql.Sql;
import com.shuimin.common.sql.SqlSelect;
import com.shuimin.pond.core.Renderable;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.exception.HttpException;

import java.util.List;
import java.util.Set;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 14-5-20.
 */
public abstract class ResourceService<E extends Record> {

    E _proto;

    abstract E prototype();

    private E getProto() {
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
        Set<String> fields = r.fields();
        return Sql.select(fields.toArray(new String[fields.size()])).from(tb_name)
                .where(req.getQuery(r, req));
    }

    @SuppressWarnings("unchecked")
    public Page<E> query(Request req) {
        E r = getProto();

        return DB.fire(tmpl -> {
            Page<E> page = Page.of(req);
            SqlSelect select = selectSql(req);
            if (Page.allowPage(req))
                select.offset(Page.getOffset(req))
                        .limit(Page.getLimit(req));
            List<E> data =
                    tmpl.map(r.mapper()::map, select.tuple());
            int count = tmpl.count(select.count().tuple());
            return page.fulfill(data, count);
        });

    }

    public Renderable render(String accept, Object o) {
        if (accept.startsWith("application/json")
                || accept.startsWith("text/json")) {
            return Renderable.json(o);
        }
        return Renderable.json(o);
    }

    public void delete(String id) {
        Record r = get(id);
        if (r == null) throw new HttpException(404, "Record[" + id + "] not found.");
        DB.fire(tmpl -> tmpl.del(r));
    }

    public void create(Request request) {
        @SuppressWarnings("unchecked")
        E a = (E) Record.newEntity(prototype().getClass())
                .merge(request);
        DB.fire(tmpl -> tmpl.add(a));
    }

    public void update(String id, Request request) {
        Record e = get(id);
        if (e == null) throw new HttpException(404, "Record[" + id + "] not found.");
        e.merge(request);
        DB.fire(tmpl -> tmpl.upd(e));
    }
}
