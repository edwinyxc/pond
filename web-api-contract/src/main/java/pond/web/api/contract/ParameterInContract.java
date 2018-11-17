package pond.web.api.contract;

import pond.web.EndToEndException;
import pond.web.http.HttpCtx;

import java.lang.annotation.*;
import java.util.Map;
import java.util.Optional;

public interface ParameterInContract<T extends Annotation> {

    String NAME_AS_DEFAULT = "_";

    Class<T> annotationType();

    String in();

    Optional<Object> provide(HttpCtx c, String name, Annotation a);


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Header {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Cookie {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Query {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Path {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface BodyForm {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface BodyMultipartAttribute {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface BodyMultipartUploadFile {
        String value() default NAME_AS_DEFAULT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface BodyJson {
        String value() default NAME_AS_DEFAULT;
    }

//    @Retention(RetentionPolicy.RUNTIME)
//    @Target({ElementType.PARAMETER})
//    @interface BodyXml {
//        String value() default NAME_AS_DEFAULT;
//    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Body { }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface BodyAs{
        Class<?> target() default Map.class;
    }


    ParameterInContract<Header> HEADER =
        new ParameterInContract<>() {
            @Override
            public Class<Header> annotationType() {
                return Header.class;
            }

            @Override
            public String in() {
                return "headers";
            }

            @Override
            public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
                assert a instanceof Header;
                var real = ((Header) a).value().equals(NAME_AS_DEFAULT)
                               ? name : ((Header) a).value();
                var http = (HttpCtx.Headers) c::bind;
                return Optional.of(http.headers().get(real)).map(list -> list.get(0));
            }
        };


    ParameterInContract<Cookie> COOKIE = new ParameterInContract<>() {
        @Override
        public Class<Cookie> annotationType() {
            return Cookie.class;
        }

        @Override
        public String in() {
            return "cookies";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof Cookie;
            var real = ((Cookie) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((Cookie) a).value();
            var http = (HttpCtx.Cookies) c::bind;
            return Optional.of(http.cookie(real));
        }
    };

    ParameterInContract<Query> QUERY = new ParameterInContract<>() {
        @Override
        public Class<Query> annotationType() {
            return Query.class;
        }

        @Override
        public String in() {
            return "queries";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof Query;
            var real = ((Query) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((Query) a).value();
            var http = (HttpCtx.Queries) c::bind;
            return Optional.of(http.query(real));
        }
    };

    ParameterInContract<Path> PATH = new ParameterInContract<>() {
        @Override
        public Class<Path> annotationType() {
            return Path.class;
        }

        @Override
        public String in() {
            return "path";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof Path;
            var real = ((Path) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((Path) a).value();
            var http = (HttpCtx.Queries) c::bind;
            return Optional.of(http.inUrlParams().get(real)).map(l -> l.get(0));
        }
    };


    ParameterInContract<Body> BODY = new ParameterInContract<>() {
        @Override
        public Class<Body> annotationType() {
            return Body.class;
        }

        @Override
        public String in() {
            return "body";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof Body;
            var http = (HttpCtx.Body) c::bind;
            return Optional.of(http.bodyAsRaw());
        }
    };


    ParameterInContract<BodyForm> BODY_FORM = new ParameterInContract<>() {
        @Override
        public Class<BodyForm> annotationType() {
            return BodyForm.class;
        }

        @Override
        public String in() {
            return "body-form";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof BodyForm;
            var real = ((BodyForm) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((BodyForm) a).value();
            var http = (HttpCtx.Body) c::bind;
            return Optional.of(http.bodyAsForm().get(real)).map(l -> l.get(0));
        }
    };

    ParameterInContract<BodyJson> BODY_JSON = new ParameterInContract<>() {
        @Override
        public Class<BodyJson> annotationType() {
            return BodyJson.class;
        }

        @Override
        public String in() {
            return "body-json";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof BodyJson;
            var real = ((BodyJson) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((BodyJson) a).value();
            var http = (HttpCtx.Body) c::bind;
            return Optional.of(http.bodyAsJson().get(real));
        }
    };

    ParameterInContract<BodyMultipartAttribute> BODY_MULTIPART_ATTRIBUTE = new ParameterInContract<>() {
        @Override
        public Class<BodyMultipartAttribute> annotationType() {
            return BodyMultipartAttribute.class;
        }

        @Override
        public String in() {
            return "body-multipart-attribute";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof BodyMultipartAttribute;
            var real = ((BodyMultipartAttribute) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((BodyMultipartAttribute) a).value();
            var http = (HttpCtx.Body) c::bind;
            try {
                return Optional.of(http.bodyAsMultipart().attrs().get(real)).map(l -> l.get(0));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new EndToEndException(500, e.getMessage(), e);
            }
        }
    };

    ParameterInContract<BodyMultipartUploadFile> BODY_MULTIPART_UPLOAD_FILE
        = new ParameterInContract<>() {
        @Override
        public Class<BodyMultipartUploadFile> annotationType() {
            return BodyMultipartUploadFile.class;
        }

        @Override
        public String in() {
            return "body-multipart-uploadfile";
        }

        @Override
        public Optional<Object> provide(HttpCtx c, String name, Annotation a) {
            assert a instanceof BodyMultipartUploadFile;
            var real = ((BodyMultipartUploadFile) a).value().equals(NAME_AS_DEFAULT)
                           ? name : ((BodyMultipartUploadFile) a).value();
            var http = (HttpCtx.Body) c::bind;
            try {
                return Optional.of(http.bodyAsMultipart().files().get(real)).map(l -> l.get(0));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new EndToEndException(500, e.getMessage(), e);
            }
        }
    };

}

