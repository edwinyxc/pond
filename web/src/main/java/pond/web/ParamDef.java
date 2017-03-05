package pond.web;

import pond.common.Convert;
import pond.common.f.Function;
import pond.common.f.Tuple;

import java.util.Date;

/**
 * Created by ed on 3/5/17.
 */
public class ParamDef<A> extends Tuple<String, Function<A, Ctx>> {

    private Class<A> aClass;

    protected ParamDef(String name, Class<A> aClass, Function<A, Ctx> handler) {
        super(name, handler);
        this.aClass = aClass;
    }


    public String name() {
        return super._a;
    }

    public Class<A> type() {
        return aClass;
    }

    public A get(Ctx req) {
        return super._b.apply(req);
    }

    @SuppressWarnings("unchecked")
    public ParamDef<A> required(String errMsg) {
        return new ParamDef<A>(this.name() + "_required", this.aClass, ctx -> {

            if (this.aClass.equals(Request.UploadFile.class)) {
                Request.UploadFile file = ((HttpCtx) ctx).req.file(this.name());
                if (file == null) throw new EndToEndException(400, errMsg);
                return (A) file;
            } else {
                String param = ((HttpCtx) ctx).req.paramNonBlank(this.name(), errMsg);
                if (this.aClass.equals(String.class)) return (A) param;
                if (this.aClass.equals(Integer.class)) return (A) Convert.toInt(param);
                if (this.aClass.equals(Date.class)) return (A) Convert.toDate(Convert.toLong(param));
                if (this.aClass.equals(Long.class)) return (A) Convert.toLong(param);
                if (this.aClass.equals(Double.class)) return (A) Convert.toDouble(param);
                throw new EndToEndException(400, String.format("Unsupported type: %s", this.aClass.getCanonicalName()));
            }
        });
    }

    public static ParamDef<String> param(String name) {
        return new ParamDef<>(name, String.class, ctx -> ((HttpCtx) ctx).req.param(name));
    }

    public static ParamDef<Integer> paramInt(String name) {
        return new ParamDef<>(name, Integer.class, ctx -> Convert.toInt(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Double> paramDouble(String name) {
        return new ParamDef<>(name, Double.class, ctx -> Convert.toDouble(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Date> paramDate(String name) {
        return new ParamDef<>(name, Date.class, ctx -> Convert.toDate(Convert.toLong(((HttpCtx) ctx).req.param(name))));
    }

    public static ParamDef<Long> paramLong(String name) {
        return new ParamDef<>(name, Long.class, ctx -> Convert.toLong(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Request.UploadFile> file(String name) {
        return new ParamDef<>(name, Request.UploadFile.class, ctx -> ((HttpCtx) ctx).req.file(name));
    }

    public static <X> ParamDef<X> raw(String name, Class<X> cls, Function<X, Ctx> handler) {
        return new ParamDef<X>(name, cls, handler);
    }

}
