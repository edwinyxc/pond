package pond.common;

import pond.common.config.Config;
import pond.common.f.*;
import pond.common.f.Callback.C0;
import pond.common.struc.EnumerationIterable;

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

  public static void _assertNotNull(Object... args) {
    S._for(args).each(arg -> S._assert(arg));
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
   * Create an array ranged from start to end, Inclusively.
   * S._range(0,3) --> [0,1,2,3]
   */
  public static Array<Integer> range(int start, int end) {
    Array<Integer> range = new Array<>();
    for (int i = start; i <= end; i++) {
      range.add(i);
    }
    return range;
  }

  public static <T> Array<T> array(T... data) {
    return new Array<>(data);
  }

  public static <T> Array<T> array(Iterable<T> data) {
    return new Array<>(data);
  }

  public static <T> Some<T> some(T t) {
    return Option.some(t);
  }

  public static <T> None<T> none() {
    return Option.none();
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
   * Tap to a callback  before return the limit argument
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
   */
  public static void _throw(Throwable th) {
    throw new RuntimeException(th);
  }


  /**
   * Unwrap the RuntimeException to get the cause, if the second argument set to true,
   * returns the limit available non-runtime-exception.
   */
  public static Throwable unwrapRuntimeException(Throwable e) {
    return unwrapRuntimeException(e, false);
  }

  /**
   * Unwrap the RuntimeException to get the cause, if the second argument set to true,
   * returns the limit available non-runtime-exception.
   */
  public static Throwable unwrapRuntimeException(Throwable e, boolean recursive) {
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
   * Tap on nullable object -- invoke the function bingding the limit arg if the input is not null
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
  public static <K, V> V _getOrSet(Map<K, V> m, K k, V v) {
    V got = m.get(k);
    if (got == null) {
      m.put(k, v);
      return v;
    } else {
      return got;
    }
  }

  //FOR

  public static <E> Array<E> _for(Iterable<E> c) {
    return new Array<>(c);
  }

  public static <E> Array<E> _for(E[] c) {
    return new Array<>(c);
  }

  public static <E> Array<E> _for(Enumeration<E> enumeration) {
    return new Array<>(new EnumerationIterable<>(enumeration));
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
          + String.join(",", avoidNull(_for((Iterable) o).map(S::dump), Collections.emptyList()))
          + "]";
    } else if (clazz.isArray()) {
      Object[] oArr = new Object[java.lang.reflect.Array.getLength(o)];
      for (int i = 0; i < oArr.length; i++) {
        oArr[i] = java.lang.reflect.Array.get(o, i);
      }
      return "[" + String.join(",", _for(oArr).<String>map(S::dump)) + "]";
    } else if (o instanceof Map) {
      return _for((Map) o).map((i) -> (dump(i))).val().toString();
    } else {
      return o.toString();
    }
  }

  /**
   * @return system current time as milliseconds
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
  final public static ARRAY array = new ARRAY();

  /**
   * ***************** C ********************
   */

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
      map.entrySet().stream().filter(entry -> grepFunc.apply(entry.getValue())).forEach(entry -> {
        newMap.put(entry.getKey(), entry.getValue());
      });
      return new ForMap<>(newMap);
    }

    public Entry<K, V> reduce(Function.F2<Entry<K, V>, Entry<K, V>, Entry<K, V>> reduceLeft) {
      return S.array(map.entrySet()).reduce(reduceLeft);
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

  final public static FILE file = new FILE();

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

  final public static PATH path = new PATH();
  final public static STREAM stream = new STREAM();
  final public static STRING str = new STRING();

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
   * system config
   */
  public final static Config config = Config.system;

}

