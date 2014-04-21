package com.shuimin.jtiny.core;

import com.shuimin.base.S;
import com.shuimin.base.util.logger.Logger;
import com.shuimin.jtiny.core.misc.Makeable;
import com.shuimin.jtiny.core.server.jetty.JettyServer;
import com.shuimin.jtiny.core.server.netty.NettyServer;

import java.util.HashMap;

/**
 * @author ed
 */
public interface Server extends Makeable {

    public static class G extends HashMap {

        public static G instance() {
            return holder.ins;
        }

        private G init() {
            put(G_MODE, RunningMode.debug);
            put(G_LOGGER, S.logger());
            return this;
        }

        private static class holder {

            final static G ins = new G().init();
        }

        public final static String G_SERVER = "g.server";

        public final static String G_LOGGER = "g.logger";

        public final static String G_MODE = "g.mode";

        public static Server server() {
            return (Server) instance().get(G_SERVER);
        }

        public static G server(Server server) {
            instance().put(G_SERVER, server);
            return instance();
        }

        public static boolean debug() {
            return mode().equals(RunningMode.debug);
        }

        public static G mode(RunningMode mode) {
            instance().put(G_MODE, mode);
            if (mode.equals(RunningMode.debug)) {
                logger().config("default", Logger.DEBUG);
            }
            return instance();
        }

        public static RunningMode mode() {
            return (RunningMode) instance().get(G_MODE);
        }

        public static Logger logger() {
            return (Logger) instance().get(G_LOGGER);
        }

        public static void debug(Object o) {
            if (mode().equals(RunningMode.debug)) {
                logger().debug(o);
            }
        }
    }

    public static void config(String name, Object o) {
        G.instance().put(name,o);
    }

    public static Object config(String name) {
        return G.instance().get(name);
    }

    public void listen(int port);

    public static G global(){
        return G.instance();
    }

    public void stop();

    public Server use(Middleware handler);

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
