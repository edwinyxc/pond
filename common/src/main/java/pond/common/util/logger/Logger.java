package pond.common.util.logger;

import pond.common.S;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ed
 */

@Deprecated
public class Logger {

    public final static int DEBUG = 3;
    public final static int ERROR = 2;
    public final static int INFO = 1;
    public final static int FATAL = 0;
    private OutWrapper[] outs;
    private String name = "shuiminLogger";

    // public Logger(PrintStream[] pss, int level)
    // {
    // this.outs = new Out[pss.length];
    // for(int i = 0; i< pss.length; i++){
    // outs[i]=new Out();
    // outs[i].ps = pss[i];
    // outs[i].level = level;
    // }
    // }
    public Logger(Out[] outs) {
        OutWrapper[] outw = new OutWrapper[outs.length];
        for (int i = 0; i < outs.length; i++) {
            outw[i] = new OutWrapper();
            outw[i].out = outs[i];
        }
        this.outs = outw;
    }

    public static Logger create(Class c) {
        return create(c.getName());
    }

    public static Logger create(String name) {
        return holder.root.addOut(new Out() {
            @Override
            protected void _print(String s) {
                System.out.print(s);
            }
        }.name(name));
    }

    public static Logger get() {
        return holder.root;
    }

    public static Logger getDefault() {
        Out defa = new Out() {
            @Override
            protected void _print(String s) {
                System.out.print(s);
            }
        };
        defa.lvl(INFO);
        defa.name("default");
        return new Logger(new Out[]{defa});
    }

    public static Logger getDebug() {
        return holder.root.config("default", DEBUG);
    }

    public void switchOuts(Out[] outs) {
        OutWrapper[] outw = new OutWrapper[outs.length];
        for (int i = 0; i < outs.length; i++) {
            outw[i] = new OutWrapper();
            outw[i].out = outs[i];
        }
        this.outs = outw;
    }

    public OutWrapper findWrapper(String name) {
        OutWrapper o;
        for (OutWrapper out : this.outs) {
            o = out;
            S._assert(o, "o null");
            if (o.name() != null && o.name().equals(name)) {
                return o;
            }
        }
        return null;
    }

    public Logger config(String name, int level) {
        OutWrapper wrapper = findWrapper(name);
        if (wrapper != null) {
            wrapper.out.lvl(level);
        }
        return this;
    }

    public Logger addOut(Out out) {
        OutWrapper o = new OutWrapper();
        o.out = out;
        OutWrapper[] tmp = new OutWrapper[outs.length + 1];
        System.arraycopy(outs, 0, tmp, 0, outs.length);
        tmp[tmp.length - 1] = o;
        this.outs = tmp;
        return this;
    }

    public void echo(Object o) {
        for (OutWrapper out : outs) {
            out._echo(o.toString(), INFO);
        }
    }

    public void info(Object o) {
        for (OutWrapper out : outs) {
            if (out.out.lvl() >= INFO) {
                out._echo(o.toString(), INFO);
            }
        }
    }

    public void err(Object o) {
        for (OutWrapper wrapper : outs) {
            if (wrapper.out.lvl() >= ERROR) {
                wrapper._echo(o.toString(), ERROR);
            }
        }

    }

    public void fatal(Object o) {
        for (OutWrapper out : outs) {
            if (out.out.lvl() >= FATAL) {
                out._echo(o.toString(), FATAL);
            }
        }
    }

    public void debug(Object o) {
        for (OutWrapper out : outs) {
            if (out.out.lvl() >= DEBUG) {
                out._echo(o.toString(), DEBUG);
            }
        }
    }

    static class OutWrapper {

        final static Date date = new Date();
        final static private SimpleDateFormat _sdf = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss");
        protected Out out;

        public String name() {
            return out.name();
        }

        private String _switchLvl(int lvl) {
            switch (lvl) {
                case Logger.DEBUG:
                    return "[DEBUG] ";
                case Logger.ERROR:
                    return "[ERROR] ";
                case Logger.FATAL:
                    return "[FATAL] ";
                case Logger.INFO:
                    return "[INFO] ";
            }
            return "";
        }

        protected void _echo(String msg, int lvl) {
            date.setTime(System.currentTimeMillis());
            final StringBuilder sb = new StringBuilder();
            sb.append(_switchLvl(lvl));
            sb.append("[").append(_sdf.format(date));
            if (lvl >= Logger.ERROR) {
                sb.append(" ").append(_findCaller());
            }

            sb.append("] ").append(msg);
            out.println(sb.toString());
        }

        protected String _findCaller() {
            final StackTraceElement[] ste = new Throwable().getStackTrace();
            // if(ste.length > 0){
            //
            // }
            if (ste.length > 4) {
                return ste[4].getMethodName() + "@" + ste[4].getClassName();
            }
            return null;
        }

    }

    private static class holder {
        private final static Logger root = getDefault();
    }
}
