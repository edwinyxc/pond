package pond.web.api.stereotype;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import pond.common.Convert;
import pond.common.JSON;
import pond.common.S;
import pond.web.EndToEndException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public interface Type<T, R> {

    Class<T> reifiedType();
    T convert(Object t);
    R consume(T t);


    static Type<List<?>, String> jsonArray(Class<List<?>> xClass) {
        return new Type<>() {
            @Override
            public Class<List<?>> reifiedType() {
                return xClass;
            }

            @Override
            public List<?> convert(Object t) {
                return com.alibaba.fastjson.JSON.parseArray(String.valueOf(t), xClass);
            }

            @Override
            public String consume(List<?> list) {
               return com.alibaba.fastjson.JSON.toJSONString(list);
            }
        };
    }

    static Type<List<?>, String> httpArray(Class<List<?>> xClass) {
        return new Type<>() {
            @Override
            public Class<List<?>> reifiedType() {
                return xClass;
            }

            @Override
            public List<?> convert(Object t) {
                return ((List<String>) t);
            }

            @Override
            public String consume(List<?> list) {
                return com.alibaba.fastjson.JSON.toJSONString(list);
            }
        };
    }


    static <X> Type<X, String> json(Class<X> xtype) {
        return new Type<X, String>() {
            @Override
            public Class<X> reifiedType() {
                return xtype;
            }

            @Override
            public X convert(Object t) {
                return JSON.parse(String.valueOf(t), xtype);
            }

            @Override
            public String consume(X x) {
                 return JSON.stringify(x);
            }
        };
    }

    static <X> Type<X, ?> any(Class<X> type) {
        return new Type<>() {
            @Override
            public Class<X> reifiedType() {
                return type;
            }

            @Override
            public X convert(Object t) {
                return (X) t;
            }

            @Override
            public Object consume(X x) {
                return x;
            }
        };
    }

    static  <X> Type<X, ByteBuf> primitive(Class<X> type) {
        assert S._is_wrapper_type(type) || type.isPrimitive();
        return new Type<X, ByteBuf>() {
            @Override
            public Class<X> reifiedType() {
                return type;
            }

            @Override
            public X convert(Object any) {
                assert any.getClass() == type;
                if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
                    return (X) Convert.toInt(S.avoidNull(any, "0"));
                } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
                    return (X) Convert.toDouble(S.avoidNull(any, "0"));
                } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
                    return (X) Convert.toDouble(S.avoidNull(any, "0"));
                } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
                    return (X) Convert.toBoolean(any, false);
                } else if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
                    return (X) Convert.toByte(any);
                } else throw new RuntimeException("UnSupported primitive type" + type);
            }

            @Override
            public ByteBuf consume(X x) {
                if(x instanceof ByteBuf){
                    return (ByteBuf) x;
                }else if(x instanceof byte[]){
                    return Unpooled.wrappedBuffer((byte[])x);
                }else if(x instanceof Byte[]){
                    return Unpooled.wrappedBuffer((byte[]) Convert.toPrimitiveArray((Byte[])x));
                }else {
                    String out = JSON.stringify(x);
                    return Unpooled.wrappedBuffer(out.getBytes(CharsetUtil.UTF_8));
                }
            }
        };
    }

    Type<Date, String> DATE_AS_LONG = new Type<>() {
        @Override
        public Class<Date> reifiedType() {
            return Date.class;
        }

        @Override
        public Date convert(Object t) {
            Long l = Convert.toLong(t);
            return new Date(l);
        }

        @Override
        public String consume(Date date) {
            return String.valueOf(date.getTime());
        }
    };

    static Type<Date, String> dateAsString(SimpleDateFormat sdf){
        return new Type<>() {
            @Override
            public Class<Date> reifiedType() {
                return Date.class;
            }

            @Override
            public Date convert(Object t) {
                try {
                    return sdf.parse(String.valueOf(t));
                } catch (ParseException e) {
                    throw new EndToEndException(400, e.getMessage(), e);
                }
            }

            @Override
            public String consume(Date date) {
                return sdf.format(date);
            }
        };
    }


}
