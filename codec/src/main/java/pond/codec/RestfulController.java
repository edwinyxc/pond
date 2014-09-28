package pond.codec;

import pond.common.S;
import pond.common.sql.Sql;
import pond.common.sql.SqlSelect;
import pond.core.Request;
import pond.core.Response;
import pond.core.http.HttpMethod;
import pond.core.Controller;
import pond.db.DB;
import pond.db.Record;
import pond.db.RecordService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pond.common.S._for;
import static pond.core.Pond.debug;
import static pond.core.Render.*;

/**
 * Created by ed on 14-5-20.
 * <p/>
 * TODO :unit test
 */
public class RestfulController<E extends Record> extends Controller {

    protected E proto;
    protected RecordService<E> service;

    protected String SORD = "_sord";
    protected String SORDF = "_sordf";

    public RestfulController(E e) {
        this.proto = e;
        service = RecordService.build(e);
    }

    public SqlSelect sqlFromReq(Request req) {
        String tb_name = proto.table();
        Set<String> fields = proto.declaredFields();
        SqlSelect sql = Sql.select(fields.toArray(new String[fields.size()])).from(tb_name)
                .where(req.toQuery(proto.declaredFields()));
        String sord = req.param(SORD);
        String sord_f = req.param(SORDF);
        if (S.str.notBlank(sord)
                && S.str.notBlank(sord_f)
                ) {
            String order;
            if (fields.contains(sord_f)) {
                order = sord_f;
            } else
                throw new RuntimeException("sordf not valid");
            if (sord.equalsIgnoreCase("desc")) {
                sql.orderBy(order + " desc");
            } else {
                sql.orderBy(order + " asc");
            }
        }
        return sql;
    }

    public Page queryForPage(Request req) {
        return DB.fire(tmpl -> {
            Page page = Page.of(req);
            SqlSelect select = sqlFromReq(req);
            if (Page.allowPage(req))
                select.offset(Page.getOffset(req))
                        .limit(Page.getLimit(req));
            List<E> data =
                    tmpl.map(proto.mapper(), select.tuple());
            int count = tmpl.count(select.count().tuple());
            List<Map<String, Object>> view =
                    _for(data).map(Record::view).toList();
            return page.fulfill(view, count);
        });
    }

    @Mapping(value = "/", methods = {HttpMethod.GET})
    public void index(Request req, Response res) {
        String mime = getAcceptHeader(req).trim().toLowerCase();
        if (mime.startsWith("text/html"))
            res.render(view(resourcePath("index.view"), queryForPage(req)));
        else
            res.render(json(queryForPage(req)));

    }

    @Mapping(value = "/", methods = {HttpMethod.POST})
    public void create(Request req, Response res) {
        res.render(json(service.create(req.toMap())));
    }

    @Mapping(value = "/new", methods = {HttpMethod.GET})
    public void view_new(Request req, Response res) {
        res.render(view(resourcePath("new.view")));
    }

    @Mapping(value = "/${_id}/edit", methods = {HttpMethod.GET})
    public void view_edit(Request req, Response res) {
        String id = req.param("_id");
        Record o = service.get(id);
        res.render(view(resourcePath("edit.view"), o.view()));
    }

    @Mapping(value = "/${_id}", methods = {HttpMethod.GET})
    public void get(Request req, Response res) {
        String id = req.param("_id");
        String mime = getAcceptHeader(req).trim().toLowerCase();
        if (mime.startsWith("text/html")) {
            Record render = service.get(id);
            if (render != null)
                res.render(view(resourcePath("detail.view"), render.view()));
            else
                res.send(404, id + " not found.");
        } else
            res.render(json(service.get(id)));
    }

    @Mapping(value = "/${_id}", methods = {HttpMethod.PUT,HttpMethod.POST})
    public void update(Request req, Response res) {
        String id = req.param("_id");
        res.render(json(service.update(id, req.toMap())));
    }

    @Mapping(value = "/${_id}", methods = {HttpMethod.DELETE})
    public void delete(Request req, Response res) {
        String id = req.param("_id");
        res.render(text(service.delete(id)));
    }


    static String getAcceptHeader(Request req) {
        String accept = _for(req.header("Accept")).first();
        debug("Accept:" + accept);
        return S.str.notBlank(accept) ? accept : "text/html";
    }

    String resourcePath(String name) {
        return File.separator + prefix + File.separator + name;
    }

}
