package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.common.util.logger.Logger;
import com.shuimin.pond.core.misc.Config;
import com.shuimin.pond.core.misc.Makeable;
import com.shuimin.pond.core.server.jetty.JettyServer;
import com.shuimin.pond.core.server.netty.NettyServer;

import java.util.HashMap;

/**
 * @author ed
 */
public interface Server extends Makeable<Server> {

    public static class G extends HashMap<String,Object> {

        public static G instance() {
            return holder.ins;
        }

        private G init() {
            put(Global.MODE, RunningMode.debug);
            put(Global.LOGGER, S.logger());
            put(Global.ROOT,S.path.webRoot());
            return this;
        }

        private static class holder {
            final static G ins = new G().init();
        }


        public static Server server() {
            return (Server) instance().get(Global.SERVER);
        }

        public static G server(Server server) {
            instance().put(Global.SERVER, server);
            return instance();
        }

        public static boolean debug() {
            return mode().equals(RunningMode.debug);
        }

        public static G mode(RunningMode mode) {
            instance().put(Global.MODE, mode);
            if (mode.equals(RunningMode.debug)) {
                logger().config("default", Logger.DEBUG);
            }
            return instance();
        }

        public static RunningMode mode() {
            return (RunningMode) instance().get(Global.MODE);
        }

        public static Logger logger() {
            return (Logger) instance().get(Global.LOGGER);
        }

        public static void debug(Object o) {
            if (mode().equals(RunningMode.debug)) {
                logger().debug(o);
            }
        }
    }


    public static void register(Class<?> clazz, Object single ) {
        S._assertNotNull(clazz,single);
        String name = clazz.getCanonicalName();
        G.debug("set global: "+name+" = "+single.toString());
        G.instance().put(name,single);
    }

    @SuppressWarnings("unchecked")
    public static <E> E register(Class<E> clazz) {
        return (E) G.instance().get(clazz.getCanonicalName());
    }

    public static void config(String name, Object o) {
        G.debug("set global: "+name+" = "+o);
        G.instance().put(name,o);
    }

    public static Object config(String name) {
        return G.instance().get(name);
    }

    public void listen(int port);

    public static G global(){
        return G.instance();
    }

    /**
     * <p>快捷调用:开启调试模式</p>
     * @return
     */
    public default Server debug() {
        G.mode(RunningMode.debug);
        return this;
    }

    public void stop();

    public Server use(Middleware handler);

    @Override
    Makeable make(Config<Server> y);

    public static Server basis(BasicServer server) {
        switch (server) {

            case netty:
                return new NettyServer();
            case jetty:
                return new JettyServer();
            default:
                return new JettyServer();
        }
    }

    static enum RunningMode {
        normal, debug
    }

    static enum BasicServer {
        netty, jetty
    }
}
