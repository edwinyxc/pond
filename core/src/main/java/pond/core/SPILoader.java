package pond.core;


import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SPI loader
 */
public final class SPILoader {

    private ConcurrentMap<Class, Object> services
            = new ConcurrentHashMap<>();


    public <E> E service(Class<E> serviceClass) {
        @SuppressWarnings("unchecked")
        E service = (E) services.get(serviceClass);
        if (service == null) {
            service = findService(serviceClass);
            if (service == null) throw
                    new RuntimeException("service[" + serviceClass + "] not found suitable provider");
            services.putIfAbsent(serviceClass, service);
        }
        return service;
    }

    private <S> S findService(Class<S> clazz) {
        ServiceLoader<S> serviceLoader = ServiceLoader.load(clazz);
        Iterator<S> sit;
        if ((sit = serviceLoader.iterator()).hasNext()) {
            return sit.next();
        }
        return null;
    }
}
