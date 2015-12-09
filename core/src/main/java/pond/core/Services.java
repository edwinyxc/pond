package pond.core;

import pond.common.S;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 We don't want to use reflection here, instead we use a Collection Singleton
 */
public class Services {

  private static Map<String, Service> services = new HashMap<>();

  static void register(String name, Service serv) {
    if( name == null ) throw new NullPointerException("null service name");
    if( services.containsKey(name) ) {
      name += "i";
    }
    serv.name(name);
    services.put(name, serv);
  }

  static Service get(String name) {
    return services.get(name);
  }

  static List<String> list() {
    return  S._for(services.entrySet()).map(Map.Entry::getKey).toList();
  }

}
