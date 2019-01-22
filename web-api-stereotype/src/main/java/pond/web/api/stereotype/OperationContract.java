package pond.web.api.stereotype;

import pond.common.f.Callback;
import pond.web.router.HttpMethod;

import java.lang.annotation.*;

public interface OperationContract<T extends Annotation> {

    Class<T> annotationType();

    Callback<OperationObject> config(Annotation a);

    public final static String DEFAULT_PATH = "DEFAULT_PATH";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface GET{
        String path() default DEFAULT_PATH;
    }

    OperationContract<GET> GET = new OperationContract<>() {
        @Override
        public Class<GET> annotationType() {
            return GET.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a ) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.GET);
                oper.path = ((GET)a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface POST{
        String path() default DEFAULT_PATH;
    }
    OperationContract<POST> POST = new OperationContract<>() {
        @Override
        public Class<POST> annotationType() {
            return POST.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a ) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.POST);
                oper.path = ((POST)a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface PUT{
        String path() default DEFAULT_PATH;
    }
    OperationContract<PUT> PUT = new OperationContract<>() {
        @Override
        public Class<PUT> annotationType() {
            return PUT.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a ) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.PUT);
                oper.path = ((PUT)a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface DELETE{
        String path() default DEFAULT_PATH;
    }
    OperationContract<DELETE> DELETE = new OperationContract<>() {
        @Override
        public Class<DELETE> annotationType() {
            return DELETE.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.DELETE);
                oper.path = ((DELETE) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface OPTIONS{
        String path() default DEFAULT_PATH;
    }
    OperationContract<OPTIONS> OPTIONS = new OperationContract<>() {
        @Override
        public Class<OPTIONS> annotationType() {
            return OPTIONS.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.OPTIONS);
                oper.path = ((OPTIONS) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface HEAD{
        String path() default DEFAULT_PATH;
    }
    OperationContract<HEAD> HEAD = new OperationContract<>() {
        @Override
        public Class<HEAD> annotationType() {
            return HEAD.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.HEAD);
                oper.path = ((HEAD) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface CONNECT {
        String path() default DEFAULT_PATH;
    }
    OperationContract<CONNECT> CONNECT = new OperationContract<>() {
        @Override
        public Class<CONNECT> annotationType() {
            return CONNECT.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.CONNECT);
                oper.path = ((CONNECT) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TRACE {
        String path() default DEFAULT_PATH;
    }
    OperationContract<TRACE> TRACE = new OperationContract<>() {
        @Override
        public Class<TRACE> annotationType() {
            return TRACE.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.mask(HttpMethod.TRACE);
                oper.path = ((TRACE) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ALL {
        String path() default DEFAULT_PATH;
    }
    OperationContract<ALL> ALL = new OperationContract<>() {
        @Override
        public Class<ALL> annotationType() {
            return ALL.class;
        }

        @Override
        public Callback<OperationObject> config(Annotation a) {
            return oper -> {
                oper.httpMask = HttpMethod.maskAll();
                oper.path = ((ALL) a).path();
            };
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface DEFAULT { }
}
