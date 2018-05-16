package pond.common;

import pond.common.config.Config;
import pond.common.f.*;
import pond.common.f.Callback.C0;
import pond.common.struc.EnumerationIterable;

import java.util.*;
import java.util.Map.Entry;

public class S {

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
   * Assert an object that is ought to be a non-null value,
   * or throw an RuntimeException.
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
   * @see S#_assert(boolean, String)
   */
  public static void _assert(boolean b) {
    _assert(b, "assert failure, something`s wrong");
  }

  /**
   * @see S#_assert(boolean, String)
   */
  public static void _assert(boolean a, String err) {
    if (a) {
      return;
    }
    throw new RuntimeException(err);
  }

  /**
   * Assert all inputs to be certainly not null
   */
  public static void _assertNotNull(Object... args) {
    S._for(args).each(a -> {if(null == a) throw new NullPointerException();});
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

  public static <T> T[] join(T... data) {
    return S._for(data).join();
  }

  /**
   * Create an ArrayList for input ordered by the input sequence.
   */
  @SafeVarargs
  public static <T> Array<T> array(T... data) {
    return new Array<>(data);
  }

  /**
   * Create an ArrayList for input ordered by the input sequence.
   */
  public static <T> Array<T> array(Iterable<T> data) {
    return new Array<>(data);
  }


  /**
   * print things to System#out
   * see {@link System#out}
   */
  public static void echo(Object... args) {
    for (Object o : args) {
      System.out.print(dump(o));
      System.out.print(" ");
    }
    System.out.println();
  }

  /**
   * @see Collection#contains(Object)
   *
   * @param some -- values to be checked
   * @param array -- data list
   * @return if the value in the data
   */
  public static boolean _in(Object some, Object... array) {
    _assert(some);
    for (Object o : array) {
      if (some.equals(o)) return true;
    }
    return false;
  }

  //inner data register
  private static Set<String> debugModeReg = new HashSet<>();

  /**
   * Switch on a Logger
   * @param clz
   */
  public static void _debug_on(Class<?>... clz) {
    debugModeReg.addAll(_for(clz).map(Class::getCanonicalName).toList());
  }

  /**
   * Switch off a Logger
   * @param clz
   */
  public static void _debug_off(Class<?>... clz) {
    debugModeReg.removeAll(_for(clz).map(Class::getCanonicalName).toList());
  }

  //TODO may be a function to redirect Logger

  /**
   * Debug lambda caller, called when the Logger is switched on
   */
  public static void _debug(org.slf4j.Logger logger,
                            Callback<org.slf4j.Logger> debugger) {
    if (debugModeReg.contains(logger.getName()))
      debugger.apply(logger);
  }

  /**
   * Quick wrapper for try catch, WARN: this function WILL catch ALL the
   * Exceptions thrown by the callback then throw a new RuntimeException wrapping them.
   * @param cb
   */
  public static void _try(Callback.C0ERR cb) {
    try {
      cb.apply();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Same as the S#_try except this function takes a Function and return the result.
   * @see
   */
  public static <R> R _try_ret(Function.F0ERR<R> f) {
    try {
      return f.apply();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Run a Callback for any times
   */
  public static void _repeat(C0 c, int times) {
    for (int i = 0; i < times; i++) {
      c.apply();
    }
  }

  /**
   * Quick Throw -- Throw a new RuntimeException
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
   * Quick Fail --  fail with hint
   */
  public static <T> T _fail(String err) {
    throw new RuntimeException(err);
  }

  /**
   * Quick Fail -- fail immediately -- used for testing & unreachable code
   */
  public static <T> T _fail() {
    throw new RuntimeException("S._fail has been triggered");
  }

  /**
   * avoid null value, return 2nd argument when the 1st is evaluated to be null.
   */
  public static <T> T avoidNull(T _check, T _else) {
    return _check == null ? _else : _check;
  }

  /**
   * Call a interceptor on the return-value before its return.
   */
  public static <E> E _tap(E e, Callback<E> interceptor) {
    interceptor.apply(e);
    return e;
  }

  /**
   * Tap on nullable object, execute the interceptor function only when the first argument is not null.
   */
  public static <R, N> R _tap_nullable(N nullable, Function<R, N> ifNotNull) {
    return nullable == null ? null : ifNotNull.apply(nullable);
  }

  public static <R, N> R avoidNull(N nullable, Function<R, N> ifNotNull) {
    return _tap_nullable(nullable, ifNotNull);
  }


  /**
   * Try to get a value from the map.
   * If the map does not have the value, Set a new one.
   */
  public static <K, V> V _getOrSet(Map<K, V> m, K k, V v) {
    V got = m.get(k);
    if (got == null) {
      m.put(k, v);
      return v;
    } else {
      return got;
    }
  }

  public static <O, E> E _wrap(O e, Function<E, O> adapter) {
      return adapter.apply(e);
  }

  //FOR -- THE FOR ITERATION

  /**
   * Create a "for" iteration
   */
  public static <E> Array<E> _for(Iterable<E> c) {
    return new Array<>(c);
  }

  /**
   * Create a "for" iteration
   */
  public static <E> Array<E> _for(E[] c) {
    return new Array<>(c);
  }

  /**
   * Create a "for" iteration
   */
  public static <E> Array<E> _for(Enumeration<E> enumeration) {
    return new Array<>(new EnumerationIterable<>(enumeration));
  }

  /**
   *
   * Create a "for" iteration -- map version
   */
  public static <K, V> ForMap<K, V> _for(Map<K, V> c) {
    return new ForMap<>(c);
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<?> clazz) throws InstantiationException,
      IllegalAccessException {
    return (T) clazz.newInstance();
  }

  /**
   * ***************** B ********************
   */

  @SuppressWarnings({"unchecked","rawtypes"})
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

  final public static ARRAY array = new ARRAY();

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

  static class map {

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> hashMap(Object[][] kv) {
      if (kv == null) {
        return new HashMap<K,V>();
      }
      HashMap<K, V> ret = new HashMap<>();
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

