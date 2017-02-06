package pond.core;

import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static pond.common.f.Tuple.pair;

/**
 * We don't want to use reflection here, instead we use a Collection Singleton
 */
public class Services {

  public final static Map<String, Service> services = new HashMap<>();

 // private final static Map
  //top-level-domain class
  //the function is designed as Function<Service, ?>

  private final static 
  List<Tuple<Class<?>, Function<? extends Service, ?>>> 
    adapters = new LinkedList<>();

  public static void add(String name, Service serv) {
    if (name == null) throw new NullPointerException("null service name");
    if (services.containsKey(name)) {
      name += "i";
    }
    serv.name(name);
    services.put(name, serv);
  }

  /**
   * 注册适配器：要求该类型必须是一个顶层可鉴别类型（注意java的类型擦除机制！）
   * @param clz 要注册的类型
   * @param adapter 绑定的适配器
   */
  public static void adapter(Class<?> clz, Function<? extends Service, ?> adapter) {
    adapters.add(pair(clz, adapter));
  }

  static Function<? extends Service, ?> search_adapter(Class<?> serv_cls ){
    Function<? extends Service, ?> adapter = null;
    for(Tuple<Class<?>, Function<? extends Service, ?>> t: adapters){
      Class<?> cls = t._a;
      Function<? extends Service, ?> f = t._b;
      if(cls.isAssignableFrom(serv_cls)) {
        adapter = f;
        break;
      }
    }
    if (adapter == null)
      throw new RuntimeException("Can not find adapter for class " + serv_cls);
    return adapter;
  }

  public static Service get(String name) {

    Object raw_serv = services.get(name);
    S._assert(raw_serv != null);

    return adapt(raw_serv);
  }

  @SuppressWarnings("all")
  public static Service adapt(Object raw_serv) {

    Class<?> raw_serv_class = raw_serv.getClass();

    if (raw_serv_class.equals(Service.class)) {
      return (Service) raw_serv;
    }

    Function adapter = search_adapter(raw_serv_class);

    //adapt raw_serv to the certain type of Service
    return (Service) adapter.apply(raw_serv);
  }

  public static List<String> all() {
    return S._for(services.entrySet()).map(Map.Entry::getKey).toList();
  }

}
