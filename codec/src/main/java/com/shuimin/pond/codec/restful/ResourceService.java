package com.shuimin.pond.codec.restful;

import com.shuimin.common.f.Tuple;
import com.shuimin.pond.codec.db.DB;
import com.shuimin.pond.codec.db.Page;
import com.shuimin.pond.codec.db.Record;
import com.shuimin.pond.codec.sql.Criterion;
import com.shuimin.pond.codec.sql.Sql;
import com.shuimin.pond.codec.sql.SqlSelect;
import com.shuimin.pond.core.Renderable;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.exception.HttpException;

import java.util.Arrays;
import java.util.List;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 14-5-20.
 */
public interface ResourceService<E extends Record> {

    E prototype();

    @SuppressWarnings("unchecked")
    default E get(String id) {
        Record r = prototype();
        String tableName = r.table();
        String pkLbl = r.PKLabel();
        SqlSelect select =
                Sql.select().from(tableName).where(
                        pkLbl, Criterion.EQ, id);
        List<E> l = DB.fire(tmpl ->
                tmpl.map(r.mapper()::map, select.tuple()));
        return _for(l).first();
    }

    default SqlSelect selectSql(Request req) {
        E r = prototype();
        String tb_name = r.table();
        return Sql.select(r.fields().toArray(new String[0])).from(tb_name)
                .where(Criterion.parseFromRequest(r, req));
    }

    @SuppressWarnings("unchecked")
    default Page<E> query(Request req) {
        E r = prototype();
        String tableName = r.table();

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

    default Renderable render(String accept, Object o) {
        if (accept.startsWith("application/json")
                || accept.startsWith("text/json")) {
            return Renderable.json(o);
        }
        return Renderable.json(o);
    }

    default void delete(String id) {
        Record r = get(id);
        if (r == null) throw new HttpException(404, "Record[" + id + "] not found.");
        DB.fire(tmpl -> tmpl.del(r));
    }

    default void create(Request request) {
        @SuppressWarnings("unchecked")
        E a = (E) Record.newEntity(prototype().getClass())
                .merge(request);
        DB.fire(tmpl -> tmpl.add(a));
    }

    default void update(String id, Request request) {
        Record e = get(id);
        if (e == null) throw new HttpException(404, "Record[" + id + "] not found.");
        e.merge(request);
        DB.fire(tmpl -> tmpl.upd(e));
    }
}
