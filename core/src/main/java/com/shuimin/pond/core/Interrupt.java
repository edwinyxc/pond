package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.pond.core.exception.PondException;

/**
 * @author ed
 */
public interface Interrupt {


    public static void redirect(String uri) {
        throw new RedirectInterruption(uri);
    }

    public static void render(Renderable o) {
        throw new RenderInterruption(o);
    }

    public static void kill() {
        throw new KillInterruption();
    }

//    public static void jump() {
//        throw new JumpInterruption();
//    }

//    public static void jump(Object cause) {
//        throw new JumpInterruption(cause);
//    }

    public static abstract class Interruption extends PondException {
        private static final Object fakeCause = new Object() {
            @Override
            public String toString() {
                return "This is an interrupt with no cause";
            }
        };

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * used in a interception chain to jump out the chain
     */
    @SuppressWarnings("serial")
    @Deprecated
    public static class JumpInterruption extends Interruption {

        public JumpInterruption() {
            super();
        }

        @Override
        public String brief() {
            return "jump out ";
        }

        @Override
        public String detail() {
            return "jump out from " + getCause();
        }

    }

    /**
     * used in a interception chain to jump out the chain
     */
    @SuppressWarnings("serial")
    public static class KillInterruption extends Interruption {

//        public KillInterruption(Object cause) {
//            super(cause);
//        }

        public KillInterruption() {
            super();
        }

        @Override
        public String brief() {
            return "kill ";
        }

        @Override
        public String detail() {
            return "kill by " + getCause();
        }

    }

    @SuppressWarnings("serial")
    public static class RedirectInterruption extends Interruption {

        final private String _uri;

        public RedirectInterruption(String uri) {
            super();
            _uri = S._notNull(uri);
        }

        public String uri() {
            return _uri;
        }

        @Override
        public String brief() {
            return "redirect to " + _uri;
        }

        @Override
        public String detail() {
            return brief() + " fired by " + getCause();
        }
    }

    @SuppressWarnings("serial")
    public static class RenderInterruption extends Interruption {

        final private Renderable value;

        public RenderInterruption(Renderable o) {
            super();
            value = o;
        }

        public Renderable value() {
            return value;
        }

        @Override
        public String brief() {
            return "render view " + value;
        }

        @Override
        public String detail() {
            return brief() + " fired by " + getCause();
        }
    }

}
