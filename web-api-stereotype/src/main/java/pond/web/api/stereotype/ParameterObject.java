package pond.web.api.stereotype;

import pond.common.S;
import pond.common.SPILoader;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.core.Entry;
import pond.web.EndToEndException;
import pond.web.Request;
import pond.web.Response;
import pond.web.api.stereotype.spi.DefaultContractor;
import pond.web.http.HttpCtx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

public class ParameterObject {

//    public enum IN {
//        ANY, QUERIES, PATH, HEADER, COOKIE, BODY_XML, BODY_JSON, BODY_FORM, BODY_RAW, CTX
//    }
//
//    public enum SCHEMA {
//        ANY, STR, STR_DATE, LONG_DATE, INT, BOOL, NUMBER, ARRAY, REQ, RESP, CTX
//    }

    public final Entry entry;
    public final Class type;
    public final String name;
    ParameterSchemaContract schema;
    ParameterInContract in;
    String description;
    boolean required;
    boolean deprecated;
    boolean allowEmptyValue;
    String style;
    boolean explode;
    boolean allowReserved;
    SchemaObject schemaObject; //TODO dup-name
    //Object example;
    Map<String, ExampleObject> examples;



    public final CtxHandler provider;

    public ParameterObject(
        String name,
        Class type,
        ParameterInContract in,
        Annotation in_anno,
        ParameterSchemaContract schema,
        Annotation schema_anno
    ){
        this.name = (in != null) ? in.name(in_anno) : name;
        this.type = type;
        this.in = in;
        this.schema = schema;
        //3 specials
        if(Ctx.class.isAssignableFrom(type)){
            this.entry = Ctx.SELF;
        }else if(Request.class.isAssignableFrom(type)) {
            this.entry = HttpCtx.REQ;
        }else if(Response.class.isAssignableFrom(type)) {
            this.entry = HttpCtx.RESP;
        }else if (in == null) {
            //default to Ctx
            this.entry = new Entry<>(name);
        }else {
            //build "never-clash-name"
            this.entry = new Entry<>(name + ":"+ type.getCanonicalName() +"@" + in.in());
        }

        //build provider & converter
        this.provider =
        CtxHandler.process(
            Ctx.SELF,
            ctx -> {
                var httpCtx = (HttpCtx) ctx::bind;
                Optional<Object> _param =
                    in != null
                        ? in.provide(httpCtx, name, in_anno)
                        : Optional.of(httpCtx.get(entry));

                if(in != null && in.required(in_anno) && _param.isEmpty()){
                    //do required-func TODO default: throw a 400 -required
                    throw new EndToEndException(400, name + " is required for this procedure");
                }

                return _param.map(ret -> {
                    if(schema != null && schema_anno != null) {
                        return  schema.type(schema_anno).convert(ret);
                    }
                    return  Type.any(type).convert(ret);
                });
            },
            this.entry);
    }

    private static DefaultContractor contractor = SPILoader.service(DefaultContractor.class);

    public static ParameterObject of(Parameter parameter) {
        //
        var name = parameter.getName();
        var type = parameter.getType();
        S.echo("Got R_Param: " + name + ":" + type);
        Annotation[] attrs = parameter.getAnnotations();
        ParameterInContract in = null;
        Annotation attr_in = null;
        ParameterSchemaContract schema = null;
        Annotation attr_schema = null;

        for(Annotation a : attrs) {
            var a_type = a.getClass();
            for(var paramIn : contractor.parameterInContracts()) {
                if(paramIn.annotationType().equals(a_type)) {
                    //match
                    in = paramIn;
                    attr_in = a;
                    break;
                }
            }

            for(var paramSchema : contractor.parameterSchemaContract()) {
                if(paramSchema.annotationType().equals(a_type)) {
                    schema = paramSchema;
                    attr_schema = a;
                    break;
                }
            }
        }

        return new ParameterObject(name, type, in, attr_in, schema, attr_schema);
    }

}
