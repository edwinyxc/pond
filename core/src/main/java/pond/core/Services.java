package pond.core;

import pond.common.S;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 We don't want to use reflection here, instead we use a Collection Singleton
 */
public class Services {

  public final static Map<String, Service> services = new HashMap<>();

  public static void register(String name, Service serv) {
    if( name == null ) throw new NullPointerException("null service name");
    if( services.containsKey(name) ) {
      name += "i";
    }
    serv.name(name);
    services.put(name, serv);
  }

  public static Service get(String name) {
    return services.get(name);
  }

  public static List<String> all() {
    return  S._for(services.entrySet()).map(Map.Entry::getKey).toList();
  }

}
