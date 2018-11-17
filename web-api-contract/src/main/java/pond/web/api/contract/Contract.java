package pond.web.api.contract;

import pond.web.http.MIME;
import pond.web.router.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

public interface Contract {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Mapping {
        String value() default "";

        HttpMethod[] methods() default {
            HttpMethod.GET,
            HttpMethod.POST
        };
    }

    @interface Title {String value() default "";}
    @interface Version {String value() default "";}
    @interface Summary{ String value() default "";}
    @interface Description {String value() default "";}

    @interface Ignore {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Produces {
        String[] value() default {
            MIME.MIME_TEXT_PLAIN,
            MIME.MIME_TEXT_HTML,
            MIME.MIME_APPLICATION_JSON,
            MIME.MIME_APPLICATION_OCTET_STREAM
        };
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Consumes {
        String[] value() default {
            MIME.MIME_TEXT_PLAIN,
            MIME.MIME_TEXT_HTML,
            MIME.MIME_APPLICATION_JSON,
            "application/x-www-form-urlencoded",
            "multipart/form-data"
        };
    }

    interface RouteConfig{

        interface Methods{
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface GET{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface POST{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface PUT{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface DELETE{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface OPTIONS{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface HEAD{ }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface CONNECT { }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface TRACE { }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface ALL { }
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface DEFAULT { }
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface RoutePrefix {
            String value() default "";
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @interface Route{ String value() default ""; }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @interface ResponseType {
            Class<?> value() default String.class;
        }
    }

    interface Parameters {
        @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER}) @interface Name{ String value(); }
        @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER}) @interface Required{ String value(); }

    }


}
