package pond.common;

import pond.common.f.Callback;
import pond.common.f.Callback.C0;
import pond.common.f.Function;
import pond.common.f.Holder;
import pond.common.struc.EnumerationIterable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

public class S {

    /**
     * ***************** meta *****************
     */

    public static String author() {
        return " edwinyxc@gmail.com ";
    }

    public static String version() {
        return "this method is a joke, how can i even \"know\" myself";
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
            throw new NullPointerException(err);
        }
    }

    /**
     * see {@link System#out}
     */
    public static void echo(Object... args) {
        //logger.echo(dump(o));
        for (Object o : args) {
            System.out.print(dump(o));
            System.out.print(" ");
        }
        System.out.println();
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

    private static Set<String> debugModeReg = new HashSet<>();

    public static void _debug(org.slf4j.Logger logger,
                              Callback<org.slf4j.Logger> debugger) {
        if (debugModeReg.contains(logger.getName()))
            debugger.apply(logger);
    }

    public static void _debug_on(Class<?>... clz) {
        debugModeReg.addAll(_for(clz).map(Class::getCanonicalName).toList());
    }

    public static void _debug_off(Class<?>... clz) {
        debugModeReg.removeAll(_for(clz).map(Class::getCanonicalName).toList());
    }


    public static void _try(Callback.C0ERR cb) {
        try {
            cb.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R _try_ret(Function.F0ERR<R> f) {
        try {
            return f.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tap to a callback  before return the first argument
     */
    public static <E> E _tap(E e, Callback<E> interceptor) {
        interceptor.apply(e);
        return e;
    }

    /**
     * Run a Callback for sevaral times
     */
    public static void _repeat(C0 c, int times) {
        for (int i = 0; i < times; i++) {
            c.apply();
        }
    }

    /**
     * Throw new RuntimeException
     *
     * @param th
     */
    public static void _throw(Throwable th) {
        throw new RuntimeException(th);
    }


    /**
     * Unwrap the runtimeexception to get the cause, if the second argument set to true,
     * returns the first available non-runtime-exception.
     */
    public static Throwable _unwrapRuntimeException(Throwable e) {
        return _unwrapRuntimeException(e, false);
    }

    /**
     * Unwrap the runtimeexception to get the cause, if the second argument set to true,
     * returns the first available non-runtime-exception.
     */
    public static Throwable _unwrapRuntimeException(Throwable e, boolean recursive) {
        if (!(e instanceof RuntimeException)) {
            return e;
        }
        Throwable ret = e.getCause();
        if (recursive) {
            while (ret instanceof RuntimeException) {
                ret = ret.getCause();
            }
        }
        return ret;
    }

    /**
     * fail immediatly
     */
    public static <T> T _fail() {
        throw new RuntimeException("S._fail has been triggered");
    }

    /**
     * fail with hint
     */
    public static <T> T _fail(String err) {
        throw new RuntimeException(err);
    }

    /**
     * avoid returnning null values
     */
    public static <T> T avoidNull(T _check, T _else) {
        return _check == null ? _else : _check;
    }

    /**
     * Tap on nullable object -- invoke the function bingding the first arg if the input is not null
     */
    public static <R, N> R _tap_nullable(N nullable, Function<R, N> ifNotNull) {
        return nullable == null ? null : ifNotNull.apply(nullable);
    }

    /**
     * Please use {@link java.util.Map#getOrDefault(Object, Object)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <E> E _getOrDefault(Map m, Object c, E _default) {
        return (E) avoidNull(m.get(c), _default);
    }


    @SuppressWarnings("unchecked")
    public static <E> E _getOrSet(Map m, Object c, E e) {
        Holder<E> eHolder = new Holder<>();
        return null != (eHolder.val((E) m.get(c))) ? eHolder.val() : S._tap(e, _e -> m.put(c, _e));
    }

    //FOR

    public static <E> ForIt<E> _for(Iterable<E> c) {
        return new ForIt<>(c);
    }

    public static <E> ForIt<E> _for(E[] c) {
        return new ForIt<>(c);
    }

    public static <E> ForIt<E> _for(Enumeration<E> enumeration) {
        return new ForIt<>(new EnumerationIterable<>(enumeration));
    }

    public static <K, V> ForMap<K, V> _for(Map<K, V> c) {
        return new ForMap<>(c);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz) throws InstantiationException,
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
            throw new NullPointerException();
        }
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
                    avoidNull(_for((Iterable) o).
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
     * use now() instead
     */
    @Deprecated
    public static long time() {
        return System.currentTimeMillis();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long now_nano() {
        return System.nanoTime();
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
    public static ARRAY array = new ARRAY();

    /**
     * ***************** C ********************
     */

    /**
     *
     */
    @Deprecated
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
            if (map == null) this.map = Collections.emptyMap();
            else this.map = map;
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

    public final static class ForIt<E> {

        private final Iterable<E> iter;

        public ForIt(Iterable<E> e) {
            if (e == null) iter = Collections.emptyList();
            else iter = e;
        }

        public ForIt(E[] e) {
            if (e == null) iter = Collections.emptyList();
            else iter = list.one(e);
        }

        private <R> Collection<R> _initCollection(Class<?> itClass) {
            if (Collection.class.isAssignableFrom(itClass)
                    && !itClass.getSimpleName().startsWith("Unmodifiable")
                    && !itClass.getSimpleName().startsWith("Empty")) {
                try {
                    return S.newInstance(itClass);
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

        public E reduce(Function.F2<E, E, E> f2, E init) {
            return list.one(iter).reduceLeft(f2, init);
        }

        public E reduce(Function.F2<E, E, E> f2) {
            return list.one(iter).reduceLeft(f2, null);
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

    public static FILE file = new FILE();

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
                return reduceLeft(reduceFunc, null);
            }

            public T reduceLeft(Function.F2<T, T, T> reduceFunc, T addtionalHead) {
                if (addtionalHead != null) {
                    T result = addtionalHead;
                    if (this.size() == 0) return addtionalHead;
                    for (int i = 1; i < this.size(); i++) {
                        result = reduceFunc.apply(result, this.get(i));
                    }
                    return result;
                } else {
                    if (this.size() == 0) return null;
                    T result = this.get(0);
                    if (this.size() == 1) return result;
                    for (int i = 1; i < this.size(); i++) {
                        result = reduceFunc.apply(result, this.get(i));
                    }
                    return result;
                }
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
    }

    public static PATH path = new PATH();
    public static STREAM stream = new STREAM();
    public static STRING str = new STRING();

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
}

