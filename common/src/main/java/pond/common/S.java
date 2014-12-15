/**
 * Face of s-lib
 *
 * @author ed
 *
 */
package pond.common;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.http.Consts;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import pond.common.f.*;
import pond.common.struc.Cache;
import pond.common.struc.IterableEnumeration;
import pond.common.struc.Matrix;
import pond.common.util.cui.Rect;
import pond.common.util.logger.Logger;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import pond.common.f.Function.F0;
import pond.common.f.Callback.C0;
import sun.net.www.http.HttpClient;

public class S {

    /**
     * ***************** logger *****************
     */
    private final static Logger logger = Logger.getDefault();

    private final static Unsafe unsafe = ((Function.F0<Unsafe>) () -> {
        try {
            Field uf = Unsafe.class.getDeclaredField("theUnsafe");
            uf.setAccessible(true);
            return (Unsafe) uf.get(null);
        } catch (IllegalAccessException | NoSuchFieldException a) {
            a.printStackTrace();
        }
        return null;
    }).apply();

    public static Logger logger() {
        return logger;
    }

    /**
     * ***************** meta *****************
     */

    public static String author() {
        return " edwinyxc@gmail.com ";
    }

    public static String version() {
        return "v0.0.1 2014";
    }

    /**
     * ***************** _ *****************
     */
    /**
     * @param <T> Type val
     * @param t   value of type T
     */
    public static <T> Some<T> _some(T t) {
        return Option.some(t);
    }

    public static <T> None<T> _none() {
        return Option.none();
    }

    public static void _assert(boolean b) {
        _assert(b, "assert failure, something`s wrong");
    }

    public static void _assert(boolean a, String err) {
        if (a) {
            return;
        }
        throw new RuntimeException(err);
    }

    public static boolean _in(Object some, Object... conditions) {
        _assert(some);
        for (Object o : conditions) {
            if (some.equals(o)) return true;
        }
        return false;
    }


    public static void _try(Callback.C0ERR cb) {
        try {
            cb.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R _try(Function.F0ERR<R> f) {
        try {
            return f.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void _echo(String s) {
        echo(s);
    }

    public static String _dump(Object o) {
        return dump(o);
    }

    public static void _lazyThrow(Throwable a) {
        throw new RuntimeException(a);
    }

    public static void _throw(Throwable th) {
        unsafe.throwException(th);
    }

    public static <T> T _fail() {
        throw new RuntimeException("BUG OCCUR, CONTACT ME:" + author());
    }

    public static <T> T _fail(String err) {
        throw new RuntimeException(err);
    }

    public static <T> T _avoidNull(T t, Class<T> clazz) {
        if (t == null) return nothing.of(clazz);
        return t;
    }

    public static <T> T _notNull(T t) {
        _assert(t, "noNull assert failure");
        return t;
    }

    public static <T> T _notNull(T t, String err) {
        _assert(t, err);
        return t;
    }

    public static <T> T _notNullElse(T _check, T _else) {
        return _check != null ? _check : _else;
    }

    /**
     * if - else - then expression
     */
    public static <T> T _return(F0<Boolean> expr, F0<T> retTrue, F0<T> retFalse) {
        return expr.apply() ? retTrue.apply() : retFalse.apply();
    }

    /**
     * if - else - then statement
     */
    public static void _do(F0<Boolean> expr, C0 doTrue, C0 doFalse) {
        if (expr.apply()) doTrue.apply();
        else doFalse.apply();
    }

    public static <E> E _getOrDefault(Map m, Object c, E _default) {
        return (E) _notNullElse(m.get(c), _default);
    }

    public static <E> E _getOrSet(Map m, Object c, E e) {
        Holder<E> eHolder = new Holder<E>();
        return _return(() -> (eHolder.val = (E) m.get(c)) != null,
                () -> eHolder.val,
                () -> {
                    m.put(c, e);
                    return e;
                });
    }

    public static <E> ForIt<E> _for(Iterable<E> c) {
        return new ForIt<>(c);
    }

    public static <E> ForIt<E> _for(E[] c) {
        return new ForIt<>(c);
    }

    public static <E> ForIt<E> _for(Enumeration<E> enumeration) {
        return new ForIt<>(new IterableEnumeration<>(enumeration));
    }

    public static <K, V> ForMap<K, V> _for(Map<K, V> c) {
        return new ForMap<>(c);
    }

    @SuppressWarnings("unchecked")
    public static <T> T _one(Class<?> clazz) throws InstantiationException,
            IllegalAccessException {
        return (T) clazz.newInstance();
    }

    /**
     * Assert an Object is NonNull, if not throw an RuntimeException.
     *
     * @param a potential null value
     */
    public static void _assert(Object a) {
        if (a == null) {
            throw new RuntimeException(new NullPointerException());
        }
    }

    /**
     * Assert an object is non-null, if not throw an RuntimeException
     * with input err string.
     *
     * @param a   potential null value
     * @param err err value
     */
    public static void _assert(Object a, String err) {
        if (a == null) {
            throw new RuntimeException(err);
        }
    }

    public static void _assertNotNull(Object... x) {
        for (Object o : x) {
            if (o == null) throw new NullPointerException();
        }
    }


    /**
     * <p>Print somethings to the default logger</p>
     *
     * @param o object(s) to print
     */
    public static void echo(Object o) {
        logger.echo(dump(o));
    }

    /**
     * ***************** B ********************
     */

    @SuppressWarnings("unchecked")
    public static String dump(Object o) {
        if (o == null) return "null";
        Class clazz = o.getClass();
        if (clazz.isPrimitive()) {
            return String.valueOf(o);
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Iterable) {
            return "["
                    + String.join(",",
                    _notNullElse(_for((Iterable) o).
                                    map((i) -> (dump(i))).val(),
                            list.one()))
                    + "]";
        } else if (clazz.isArray()) {
            Object[] oArr = new Object[Array.getLength(o)];
            for (int i = 0; i < oArr.length; i++) {
                oArr[i] = Array.get(o, i);
            }
            return "["
                    + String.join(",", _for(oArr).
                    <String>map((i) -> (dump(i))).val())
                    + "]";
        } else if (o instanceof Map) {
            return _for((Map) o).map((i) -> (dump(i))).val().toString();
        } else {
            return o.toString();
        }
    }

    /**
     * ******************* L **********************
     */
    @SafeVarargs
    public static <E> list.FList<E> list(E... e) {
        return list.one(e);
    }

    /**
     * ***************** E ********************
     */

    /**
     * @return system current time as millseconds
     */
    public static long time() {
        return System.currentTimeMillis();
    }

    public static long time(Callback.C0 cb) {
        long start = System.currentTimeMillis();
        cb.apply();
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long time_nano(Callback.C0 cb) {
        long start = System.nanoTime();
        cb.apply();
        long end = System.nanoTime();
        return end - start;
    }

    /**
     * ****************** A ***************
     */
    public static class array {

        public static <T> Iterable<T> to(T[] arr) {
            return list.one(arr);
        }

        public static <T> T last(T[] array) {
            return array[array.length - 1];
        }

        public static <T> T first(T[] array) {
            return array[0];
        }

        public static <T> T[] of(Iterable<T> iter) {
            List<T> tmp = new LinkedList<>();
            for (T e : iter) {
                tmp.add(e);
            }
            return of(tmp.toArray());
        }

        public static <T> T[] of(Enumeration<T> enumeration) {
            List<T> tmp = new LinkedList<>();
            while (enumeration.hasMoreElements()) {
                tmp.add(enumeration.nextElement());
            }
            return of(tmp.toArray());
        }

        @SuppressWarnings("unchecked")
        public static <T> T[] of(Object[] arr) {
            if (arr.length == 0) {
                return (T[]) arr;
            }
            Class<?> tClass = arr[0].getClass();
            Object array = Array.newInstance(tClass, arr.length);
            for (int i = 0; i < arr.length; i++) {
                Array.set(array, i, arr[i]);
            }
            return (T[]) array;
        }

        /**
         * check if an array contains something
         *
         * @param arr array
         * @param o   the thing ...
         * @return -1 if not found or the first index of the object
         */
        public static int contains(Object[] arr, Object o) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != null && arr[i].equals(o)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * check if an array contains something
         *
         * @param arr array
         * @param o   the thing ...
         * @return -1 if not found or the first index of the object
         */
        public static int contains(int[] arr, int o) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * put elements to the left.delete the null ones
         *
         * @param arr input Object array
         * @return compacted array
         */
        public static Object[] compact(Object[] arr) {
            int cur = 0;
            int next_val = 0;
            while (next_val < arr.length) {
                if (arr[cur] == null) {
                    /* find next available */
                    for (; next_val < arr.length; next_val++) {
                        if (arr[next_val] != null) {
                            break;// get the value
                        }
                    }
                    if (next_val >= arr.length) {
                        break;
                    }
                    /* move it to the cur */
                    arr[cur] = arr[next_val];
                    arr[next_val] = null;
                    cur++;
                } else {
                    next_val++;
                    cur++;
                }
            }
            Object[] ret;
            if (arr[0] != null) {
                Class<?> c = arr[0].getClass();
                ret = (Object[]) Array.newInstance(c, cur);
            } else {
                ret = new Object[cur];
            }

            System.arraycopy(arr, 0, ret, 0, ret.length);
            return ret;
        }

        /**
         * Convert a list to an array.
         *
         * @param clazz Class of input list
         * @param list  input list
         * @return array
         */
        public static Object fromList(Class<?> clazz, List<?> list) {
            Object array = Array.
                    newInstance(clazz, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }

        /**
         * Convert an array to a new one in which every element has type converted.
         *
         * @param clazz Class to convert to
         * @param arr   input array
         * @return converted array
         */
        public static Object convertType(Class<?> clazz, Object[] arr) {
            Object array = Array.
                    newInstance(clazz, arr.length);
            for (int i = 0; i < arr.length; i++) {
                Array.set(array, i, arr[i]);
            }
            return array;
        }

    }

    /**
     * ***************** C ********************
     */

    public static class collection {

        public static class set {

            public static <E> HashSet<E> hashSet(Iterable<E> arr) {
                HashSet<E> ret = new HashSet<>();
                for (E e : arr) {
                    ret.add(e);
                }
                return ret;
            }

            public static <E> HashSet<E> hashSet(E[] arr) {
                final HashSet<E> ret = new HashSet<>();
                ret.addAll(Arrays.asList(arr));
                return ret;
            }
        }

        public static class list {

            public static <E> ArrayList<E> arrayList(Iterable<E> arr) {
                final ArrayList<E> ret = new ArrayList<>();
                for (E t : arr) {
                    ret.add(t);
                }
                return ret;
            }

            public static <E> ArrayList<E> arrayList(E[] arr) {
                final ArrayList<E> ret = new ArrayList<>();
                /*
                 for (E val : arr) {
                 ret.add(val);
                 }
                 */
                ret.addAll(Arrays.asList(arr));
                return ret;
            }

            public static <E> LinkedList<E> linkedList(Iterable<E> arr) {
                final LinkedList<E> ret = new LinkedList<>();
                for (E t : arr) ret.add(t);
                return ret;
            }

            public static <E> LinkedList<E> linkedList(E[] arr) {
                final LinkedList<E> ret = new LinkedList<>();
                ret.addAll(Arrays.asList(arr));
                return ret;
            }
        }
    }

    /**
     * ***************** D ********************
     */
    public static class date {

        public static Date fromString(String aDate, String aFormat) throws ParseException {
            return new SimpleDateFormat(aFormat).parse(aDate);
        }

        public static Date fromLong(String aDate) {
            return new Date(parse.toLong(aDate));
        }

        public static String fromLong(Long aDate, String aFormat) {
            return toString(new Date(aDate), aFormat);
        }

        public static String fromLong(String aDate, String aFormat) {
            return toString(new Date(parse.toLong(aDate)), aFormat);
        }

        public static String toString(Date aDate, String aFormat) {
            return new SimpleDateFormat(aFormat).format(aDate);
        }

        public static Long stringToLong(String aDate, String aFormat) throws ParseException {
            return fromString(aDate, aFormat).getTime();
        }
    }

    /**
     * ******************* F
     * <p>
     * *********************
     *
     * @param <K>
     * @param <V>
     */
    public final static class ForMap<K, V> {

        private final Map<K, V> map;

        protected ForMap(Map<K, V> map) {
            this.map = map;
        }

        public ForMap<K, V> filter(Function<Boolean, Entry<K, V>> grepFunc) {
            Map<K, V> newMap = S.map.hashMap(null);
            for (Entry<K, V> entry : map.entrySet()) {
                if (grepFunc.apply(entry)) {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            return new ForMap<>(newMap);
        }

        public ForMap<K, V> filterByKey(Function<Boolean, K> grepFunc) {
            Map<K, V> newMap = S.map.hashMap(null);

            for (Entry<K, V> entry : map.entrySet()) {
                if (grepFunc.apply(entry.getKey())) {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }

            return new ForMap<>(newMap);
        }

        public ForMap<K, V> filterByValue(Function<Boolean, V> grepFunc) {
            Map<K, V> newMap = S.map.hashMap(null);
            for (Entry<K, V> entry : map.entrySet()) {
                if (grepFunc.apply(entry.getValue())) {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            return new ForMap<>(newMap);
        }

        public Entry<K, V> reduce(Function.F2<Entry<K, V>, Entry<K, V>, Entry<K, V>> reduceLeft) {
            return list.one(map.entrySet()).reduceLeft(reduceLeft);
        }

        public <R> ForMap<K, R> map(Function<R, V> mapFunc) {
            Map<K, R> newMap = S.map.hashMap(null);
            for (Entry<K, V> entry : map.entrySet()) {
                newMap.put(entry.getKey(), mapFunc.apply(entry.getValue()));
            }
            return new ForMap<>(newMap);
        }

        public ForMap<K, V> each(Callback<Entry<K, V>> eachFunc) {
            map.entrySet().forEach(eachFunc::apply);
            return this;
        }

        public Map<K, V> val() {
            return map;
        }

    }

    /**
     * ******************* G **********************
     */
    /**
     * ******************* H **********************
     */
    public final static class http {

        public static  void get(String uri, Map<String,Object> params, Callback<HttpResponse> cb) {
            send(new HttpGet(uri), params, cb);
        }

        public static void post(String uri, Map<String,Object> params, Callback<HttpResponse> cb) {
            send(new HttpPost(uri), params, cb);
        }

        public static void put(String uri, Map<String,Object> params, Callback<HttpResponse> cb) {
            send(new HttpPut(uri), params, cb);
        }

        public static void delete(String uri, Map<String,Object> params, Callback<HttpResponse> cb) {
            send(new HttpDelete(uri), params, cb);
        }

        public static void send(HttpUriRequest request, Map<String,Object> params, Callback<HttpResponse> cb) {
            if(params == null) params = Collections.emptyMap();
            List<NameValuePair> dummyform = new ArrayList<>();
            _for(params).each((e) ->{
                dummyform.add(new BasicNameValuePair(e.getKey(),String.valueOf(e.getValue())));
            });

            if (request instanceof HttpPost ||
                    request instanceof HttpPut) {
                ((HttpEntityEnclosingRequest) request).setEntity(new UrlEncodedFormEntity(dummyform, Consts.UTF_8));
            } else {
                String uri = request.getURI().toString();
                uri += URLEncodedUtils.format(dummyform, Consts.UTF_8);
                ((HttpRequestBase) request).setURI(URI.create(uri));
            }

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse resp = client.execute(request);
            ) {
                cb.apply(resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * ******************* I **********************
     */
    /**
     * ******************* J **********************
     */
    /**
     * ******************* K **********************
     */

    public final static class ForIt<E> {

        private final Iterable<E> iter;

        public ForIt(Iterable<E> e) {
            iter = e;
        }

        public ForIt(E[] e) {
            iter = list.one(e);
        }

        private <R> Collection<R> _initCollection(Class<?> itClass) {
            if (Collection.class.isAssignableFrom(itClass)
                    && !itClass.getSimpleName().startsWith("Unmodifiable")
                    && !itClass.getSimpleName().startsWith("Empty")) {
                try {
                    return S._one(itClass);
                } catch (InstantiationException | IllegalAccessException e) {
                    return list.one();
                }
            } else {
                return list.one();
            }
        }

        public <R> ForIt<R> map(final Function<R, E> mapper) {
            final Class<?> itClass = iter.getClass();
            final Collection<R> result = _initCollection(itClass);
            each((e) -> {
                result.add(mapper.apply(e));
            });
            return new ForIt<>(result);
        }

        public ForIt<E> each(Callback<E> eachFunc) {
            iter.forEach(eachFunc::apply);
            return this;
        }

        public ForIt<E> filter(final Function<Boolean, E> grepFunc) {
            final Class<?> itClass = iter.getClass();
            final Collection<E> c = _initCollection(itClass);
            each((e) -> {
                if (grepFunc.apply(e)) {
                    c.add(e);
                }
            });
            return new ForIt<>(c);
        }

        public E reduce(final Function.F2<E, E, E> reduceLeft) {
            list.FList<E> l = list.one(iter);
            if (l.size() == 0) return null;
            return l.reduceLeft(reduceLeft);
        }

        public Iterable<E> val() {
            return iter;
        }

        public ForIt<E> compact() {
            return filter((e) -> (e != null));
        }

        public E first() {
            Iterator<E> it = iter.iterator();
            if (it.hasNext()) {
                return it.next();
            }
            return null;
        }

        public E[] join() {
            return array.of(iter);
        }

        public List<E> toList() {
            return list.one(this.val());
        }

        public Set<E> toSet() {
            return collection.set.hashSet(this.val());
        }

    }

    public static class file {

        /**
         * (Util method)
         * Load properties from the file
         */
        public static Properties loadProperties(File conf) {
            return _try(() -> {
                Properties config = new Properties();
                if (conf.exists() && conf.canRead())
                    config.load(new FileInputStream(conf));
                    //using default settings;
                else
                    System.out.println(
                            "Can`t read properties file, using default.");
                return config;
            });
        }

        /**
         * (Util method)
         * Load properties from the file, under the classroot
         */
        public static Properties loadProperties(String fileName) {
            return _try(() -> {
                Properties config = new Properties();
                File conf = new File(S.path.rootClassPath()
                        + File.separator + fileName);
                if (conf.exists() && conf.canRead())
                    config.load(new FileInputStream(conf));
                    //using default settings;
                else
                    System.out.println(
                            "Can`t read properties file, using default.");
                return config;
            });
        }

        public static void inputStreamToFile(InputStream ins, File file) throws IOException {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        }

        public static String fileNameFromPath(String path) {
            return path.substring(path.lastIndexOf("\\") + 1);
        }

        /**
         * Returns file extension name,
         * return null if it has no extension.
         *
         * @param fileName
         * @return
         */
        public static String fileExt(String fileName) {
            String[] filename = splitFileName(fileName);
            return filename[filename.length - 1];
        }

        public static String[] splitFileName(String filename) {
            int idx_dot = filename.lastIndexOf('.');
            if (idx_dot <= 0 || idx_dot == filename.length()) {
                return new String[]{filename, null};
            }
            return new String[]{filename.substring(0, idx_dot), filename.substring(idx_dot + 1)};
        }

        /**
         * abc.txt => [abc, txt] abc.def.txt => [abc.def, txt] abc. =>
         * [abc.,null] .abc => [.abc,null] abc => [abc,null]
         *
         * @param file file
         * @return string array with size of 2, first is the filename, remain the suffix;
         */
        public static String[] splitFileName(File file) {
            return splitFileName(file.getName());
        }

        public static File mkdir(File par, String name) throws IOException {
            final String path = par.getAbsolutePath() + File.separatorChar + name;
            File f = new File(path);
            if (f.mkdirs() && f.createNewFile()) {
                return f;
            }
            return null;
        }

        public static File touch(File par, String name) throws IOException {
            final String path = par.getAbsolutePath() + File.separatorChar + name;
            File f = new File(path);
            if (f.createNewFile()) {
                return f;
            }
            return null;
        }

        /**
         * Delete a dir recursively deleting anything inside it.
         *
         * @param file The dir to delete
         * @return true if the dir was successfully deleted
         */
        public static boolean rm(File file) {
            if (!file.exists() || !file.isDirectory()) {
                return false;
            }

            String[] files = file.list();
            for (String file1 : files) {
                File f = new File(file, file1);
                if (f.isDirectory()) {
                    rm(f);
                } else {
                    f.delete();
                }
            }
            return file.delete();
        }
    }

    final static public class list {

        public static <E> FList<E> one() {
            return new FList<>();
        }

        public static <E> FList<E> one(Iterable<E> iterable) {
            return new FList<>(iterable);
        }

        public static <E> FList<E> one(E... arr) {
            return new FList<>(arr);
        }

        @SuppressWarnings("serial")
        final static public class FList<T> extends ArrayList<T> {

            public FList() {
                super();
            }

            public FList(T[] a) {
                super();
                this.addAll(Arrays.asList(a));
            }

            public FList(List<T> a) {
                super(a);
            }

            public FList(Iterable<T> iter) {
                super();
                iter.forEach(this::add);
            }

            public FList(int i) {
                super(i);
            }

            public FList<T> slice(int start, int end) {
                final FList<T> ret = new FList<>();
                for (int i = start; i < end; i++) {
                    ret.add(this.get(i));
                }
                return ret;
            }

            public FList<T> slice(int start) {
                final FList<T> ret = new FList<>();
                for (int i = start; i < size(); i++) {
                    ret.add(this.get(i));
                }
                return ret;
            }

            public T reduceLeft(Function.F2<T, T, T> reduceFunc) {
                if (this.size() == 1) {
                    return this.get(0);
                }
                T result = this.get(0);
                for (int i = 1; i < this.size(); i++) {
                    result = reduceFunc.apply(result, this.get(i));
                }
                return result;
            }

            public String join(String sep) {
                final StringBuilder sb = new StringBuilder();
                for (T t : this) {
                    sb.append(t.toString()).append(sep);
                }
                return sb.toString();
            }
        }

    }

    /**
     * ******************* M **********************
     */
    public static class math {

        public static int max(int a, int b) {
            return a > b ? a : b;
        }
    }

    public static class map {

        @SuppressWarnings("unchecked")
        public static <K, V> HashMap<K, V> hashMap(Object[][] kv) {
            if (kv == null) {
                return new HashMap();
            }
            HashMap<K, V> ret = new HashMap();
            for (Object[] entry : kv) {
                if (entry.length >= 2) {
                    ret.put((K) entry[0], (V) entry[1]);
                }
            }
            return ret;
        }
    }

    public static class matrix {

        public static Matrix console(int maxLength) {
            return new Matrix(0, maxLength);
        }

        /**
         * <p>
         * Print a matrix whose each row as a String.
         * </p>
         *
         * @return a string represent the input
         * matrix using '\n' to separate between lines
         */
        public static String mkStr(Rect r) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < r.height; i++) {
                for (int j = 0; j < r.width; j++) {
                    char c = (char) r.data.get(i, j);
                    sb.append(c);
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        public static Matrix addHorizontal(Matrix... some) {
            int height = 0;
            int width = 0;
            for (Matrix x : some) {
                height = math.max(height, x.rows());
                width += x.cols();
            }

            int[][] out = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    out[i][j] = ' ';
                }
            }

            int colfix = 0;
            for (Matrix x : some) {
                for (int h = 0; h < x.rows(); h++) {
                    int[] row = x.row(h);
                    for (int _i = 0; _i < row.length; _i++) {
                        out[h][colfix + _i] = (char) row[_i];
                    }
                }
                colfix += x.cols();
            }
            return new Matrix(out);
        }

        public static Matrix fromString(String... s) {
            S.echo(s);
            final int maxLen = list.one(array.<String>of(array.compact(s))).reduceLeft(
                    (String a, String b) -> {
                        if (a == null || b == null) {
                            return "";
                        }
                        return a.length() > b.length() ? a : b;
                    }
            ).length();

            int[][] ret = new int[s.length][maxLen];
            for (int i = 0; i < s.length; i++) {
                for (int j = 0; j < s[i].length(); j++) {
                    ret[i][j] = s[i].charAt(j);
                }
            }
            return new Matrix(ret);
        }
    }

    /**
     * ******************* O **********************
     */
    /**
     * ******************* P **********************
     */

    /**
     * ******************* N **********************
     */
    final public static class nothing {

        public final static Boolean _boolean = Boolean.FALSE;
        public final static Character _char = (char) '\0';
        public final static Byte _byte = (byte) 0;
        public final static Integer _int = 0;
        public final static Short _short = (short) 0;// ???
        public final static Long _long = (long) 0;
        public final static Float _float = (float) 0;
        public final static Double _double = (double) 0;

        final static Cache<Class<?>, Object> _nothingValues = Cache.<Class<?>, Object>defaultCache().onNotFound(
                a -> Enhancer.create(_notNull(a), (MethodInterceptor) (Object obj, Method method, Object[] args, MethodProxy proxy) -> null));

        static {
            _nothingValues.put(String.class, "")
                    .put(Boolean.class, _boolean).put(Integer.class, _int)
                    .put(Byte.class, _byte).put(Character.class, _char).put(Short.class, _short).put(Long.class, _long)
                    .put(Float.class, _float).put(Double.class, _double).put(Object.class, new Object());
        }

        final private Class<?> proxyClass;

        protected nothing(Class<?> clazz) {
            proxyClass = clazz;
        }

        @SuppressWarnings("unchecked")
        public static <T> T of(Class<T> t) {
            return (T) new nothing(t).proxy();
        }

        protected Object proxy() {
            return _nothingValues.get(proxyClass);
        }

    }

    /**
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    final public static class proxy<T> {

        final Map<String, Function<Object, Object[]>> _mm = new HashMap<>(5);
        final private Class<T> _clazz;
        final private T _t;

        private proxy(Class<T> clazz, T t) {
            _clazz = clazz;
            _t = t;
        }

        public static <T> proxy<T> one(Class<T> clazz) throws InstantiationException, IllegalAccessException {
            return new proxy(clazz, S.<T>_one(clazz));
        }

        public static <T> proxy<T> of(T t) {
            return new proxy(t.getClass(), t);
        }

        public T origin() {
            return _t;
        }

        public Class<T> originClass() {
            return _clazz;
        }

        public proxy<T> method(String methodName, Function<Object, Object[]> method) {
            _mm.put(methodName, method);
            return this;
        }

        public T create() {
            return (T) Enhancer.create(_clazz,
                    (MethodInterceptor) (obj, method, args, proxy) ->
                            _avoidNull(_mm.get(method.getName()), Function.class).apply(args)
            );

        }
    }

    public static class path {


        @SuppressWarnings("ConstantConditions")
        public static String rootAbsPath(Object caller) {
            return caller.getClass().getClassLoader().getResource("/").getPath();
        }

        @SuppressWarnings("ConstantConditions")
        public static String rootAbsPath(Class<?> callerClass) {
            return callerClass.getClassLoader().getResource("/").getPath();
        }

        @SuppressWarnings("rawtypes")
        public static String get(Class clazz) {
            String path = clazz.getResource("").getPath();
            return new File(path).getAbsolutePath();
        }

        public static String get(Object object) {
            String path = object.getClass().getResource("").getPath();
            return new File(path).getAbsolutePath();
        }

        @SuppressWarnings("ConstantConditions")
        public static String rootClassPath() {
            try {
                String path = S.class.getClassLoader().getResource("").toURI().getPath();
                return new File(path).getAbsolutePath();
            } catch (URISyntaxException e) {
                String path = S.class.getClassLoader().getResource("").getPath();
                return new File(path).getAbsolutePath();
            }
        }

        public static String packageOf(Object object) {
            Package p = object.getClass().getPackage();
            return p != null ? p.getName().replaceAll("\\.", "/") : "";
        }

        /**
         * Normally return the source dir path under the current project
         *
         * @return the source dir path under the current project
         */
        public static String detectWebRootPath() {
            try {
                String path = S.class.getResource("/").toURI().getPath();
                return new File(path).getParentFile().getParentFile().getCanonicalPath();
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Boolean isAbsolute(String path) {
            _assert(path);
            return path.startsWith("/") ||
                    path.indexOf(":") == 1;
        }

    }

    /**
     * ******************* Q **********************
     */
    /**
     * ******************* R **********************
     */

//    public static class reflect {
//        public static boolean isPrimitive(Object o){
//            Class c = o.getClass();
//            //TODO?
//            return c.isPrimitive();
//        }
//    }

    /**
     * ******************* S **********************
     */

    public static class parse {

        /**
         * <p>
         * WARNING!!! ONLY POSITIVE VALUES WILL BE RETURN
         * </p>
         *
         * @param value input value
         * @return above zero
         */
        public static int toUnsigned(String value) {
            int ret = 0;
            if (value == null || value.isEmpty()) {
                return 0;
            }
            char tmp;
            for (int i = 0; i < value.length(); i++) {
                tmp = value.charAt(i);
                if (!Character.isDigit(tmp)) {
                    return 0;
                }
                ret = ret * 10 + ((int) tmp - (int) '0');
            }
            return ret;
        }

        public static long toLong(String value) throws NumberFormatException {
            return Long.parseLong(value);
        }
    }

    /**
     * @author ed
     */
    public static class stream {

        private static final int BUFFER_SIZE = 8192;

        public static String readFully(InputStream inputStream, Charset encoding)
            throws IOException {
            return new String(readFully(inputStream), encoding);
        }

        public static String readFully(InputStream inputStream, String encoding)
                throws IOException {
            return new String(readFully(inputStream), encoding);
        }

        private static byte[] readFully(InputStream inputStream)
                throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        }

        /**
         * write from is to os;
         *
         * @param in  inputStream
         * @param out outputStream
         * @throws java.io.IOException
         */
        public static void write(final InputStream in, final OutputStream out) throws IOException {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int cnt;

            while ((cnt = in.read(buffer)) != -1) {
                out.write(buffer, 0, cnt);
//            }
            }
        }
    }

    /**
     * ******************* T **********************
     */

    public static class str {

        public static final String EMPTY = "";
        public static final String[] EMPTY_STR_ARRAY = new String[]{};

        public static final String NEWLINE;

        static {
            String newLine;

            try {
                newLine = new Formatter().format("%n").toString();
            } catch (Exception e) {
                newLine = "\n";
            }

            NEWLINE = newLine;
        }

        public static boolean isBlank(String str) {
            return str == null || "".equals(str.trim());
        }

        public static boolean notBlank(String str) {
            return str != null && !"".equals(str.trim());
        }

        public static boolean notBlank(String... strings) {
            if (strings == null) {
                return false;
            }
            for (String str : strings) {
                if (str == null || "".equals(str.trim())) {
                    return false;
                }
            }
            return true;
        }

        public static boolean notNull(Object... paras) {
            if (paras == null) {
                return false;
            }
            for (Object obj : paras) {
                if (obj == null) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @param ori original string
         * @param ch  char
         * @param idx index of occurrence of specified char
         * @return real index of the input char
         */
        public static int indexOf(String ori, char ch, int idx) {
            char c;
            int occur_idx = 0;
            for (int i = 0; i < ori.length(); i++) {
                c = ori.charAt(i);
                if (c == ch) {
                    if (occur_idx == idx) {
                        return i;
                    }
                    occur_idx++;
                }
            }
            return -1;
        }

        /**
         * Generates a camel case version of a phrase from underscore.
         *
         * @param underscore underscore version of a word to converted to camel case.
         * @return camel case version of underscore.
         */
        public static String camelize(String underscore) {
            return camelize(underscore, false);
        }

        public static String pascalize(String underscore) {
            return camelize(underscore, true);
        }


        /**
         * Generates a camel case version of a phrase from underscore.
         *
         * @param underscore          underscore version of a word to converted to camel case.
         * @param capitalizeFirstChar set to true if first character needs to be capitalized, false if not.
         * @return camel case version of underscore.
         */
        public static String camelize(String underscore, boolean capitalizeFirstChar) {
            String result = "";
            StringTokenizer st = new StringTokenizer(underscore, "_");
            while (st.hasMoreTokens()) {
                result += capitalize(st.nextToken());
            }
            return capitalizeFirstChar ? result : result.substring(0, 1).toLowerCase() + result.substring(1);
        }

        /**
         * Capitalizes a word  - only a first character is converted to upper case.
         *
         * @param word word/phrase to capitalize.
         * @return same as input argument, but the first character is capitalized.
         */
        public static String capitalize(String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }

        /**
         * Converts a CamelCase string to underscores: "AliceInWonderLand" becomes:
         * "alice_in_wonderland"
         *
         * @param camel camel case input
         * @return result converted to underscores.
         */
        public static String underscore(String camel) {

            List<Integer> upper = new ArrayList<Integer>();
            byte[] bytes = camel.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                if (b < 97 || b > 122) {
                    upper.add(i);
                }
            }

            StringBuffer b = new StringBuffer(camel);
            for (int i = upper.size() - 1; i >= 0; i--) {
                Integer index = upper.get(i);
                if (index != 0)
                    b.insert(index, "_");
            }

            return b.toString().toLowerCase();

        }

    }

    /**
     * ******************* U **********************
     */
    public static class uuid {

        public static String str() {
            return UUID.randomUUID().toString();
        }

        public static UUID base() {
            return UUID.randomUUID();
        }

        public static String vid() {
            UUID uuid = UUID.randomUUID();
            return uuid.toString().replaceAll("-", "");
        }
    }
    /**
     ******************* V **********************
     */
    /**
     * ******************* W **********************
     */
    /**
     * ******************* X **********************
     */
    /**
     * ******************* Y **********************
     */
    /**
     * ******************* Z **********************
     */
}

