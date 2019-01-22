package pond.web.api.stereotype;

import java.lang.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ParameterSchemaContract<T extends Annotation, R> {

    Class<T> annotationType();

    Type<R, ?> type(Annotation a);


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface LongToDate {
    }

    ParameterSchemaContract<LongToDate, Date> LONG_TO_DATE = new ParameterSchemaContract<>() {
        @Override
        public Class<LongToDate> annotationType() {
            return LongToDate.class;
        }

        @Override
        public Type<Date, String> type(Annotation a) {
            return Type.DATE_AS_LONG;
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface StringToDate {
        String value() default "yyyy-MM-dd HH:mm:ss";
    }

    ParameterSchemaContract<StringToDate, Date> STR_TO_DATE = new ParameterSchemaContract<>() {
        @Override
        public Class<StringToDate> annotationType() {
            return StringToDate.class;
        }

        @Override
        public Type<Date, String> type(Annotation a) {
            assert a instanceof StringToDate;
            String format = ((StringToDate) a).value();
            return Type.dateAsString(new SimpleDateFormat(format));
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Number {
    }

    ParameterSchemaContract<Number, Double> NUMBER =
        new ParameterSchemaContract<>() {
            @Override
            public Class<Number> annotationType() {
                return Number.class;
            }

            @Override
            public Type<Double, ?> type(Annotation a) {
                return Type.primitive(Double.class);
            }
        };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Int {
    }

    ParameterSchemaContract<Int, Integer> INT = new ParameterSchemaContract<>() {
        @Override
        public Class<Int> annotationType() {
            return Int.class;
        }

        @Override
        public Type<Integer, ?> type(Annotation a) {
            return Type.primitive(Integer.class);
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface Long {
    }

    ParameterSchemaContract<Long, java.lang.Long> LONG = new ParameterSchemaContract<>() {
        @Override
        public Class<Long> annotationType() {
            return Long.class;
        }

        @Override
        public Type<java.lang.Long, ?> type(Annotation a) {
            return Type.primitive(java.lang.Long.class);
        }
    };


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface JSON {
        Class<?> type() default Map.class;
    }

    ParameterSchemaContract<JSON, ?> JSON =
        new ParameterSchemaContract<>() {
            @Override
            public Class<JSON> annotationType() {
                return JSON.class;
            }

            @Override
            public Type type(Annotation a) {
                assert a instanceof JSON;
                Class<?> format = ((JSON) a).type();
                return Type.json(format);
            }
        };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface JSON_ARRAY {
        Class<List<?>> type();
    }

    ParameterSchemaContract<JSON_ARRAY, List<?>> JSON_ARRAY =
    new ParameterSchemaContract<>() {
        @Override
        public Class<JSON_ARRAY> annotationType() {
                                                return JSON_ARRAY.class;
                                                                        }

        @Override
        public Type<List<?>, String> type(Annotation a) {
            assert a instanceof JSON_ARRAY;
            Class<List<?>> format = ((JSON_ARRAY) a).type();
            return Type.jsonArray(format);
        }
    };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @interface HTTP_ARRAY{
        Class<List<?>> type();
    }

    ParameterSchemaContract<HTTP_ARRAY, List<?>> HTTP_ARRAY =
        new ParameterSchemaContract<>() {
            @Override
            public Class<HTTP_ARRAY> annotationType() {
                return HTTP_ARRAY.class;
            }

            @Override
            public Type<List<?>, String> type(Annotation a) {
                assert a instanceof HTTP_ARRAY;
                Class<List<?>> format = ((HTTP_ARRAY) a).type();
                return Type.httpArray(format);
            }
        };
}


