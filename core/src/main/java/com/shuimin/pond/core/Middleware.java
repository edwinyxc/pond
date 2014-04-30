package com.shuimin.pond.core;

import com.shuimin.pond.core.misc.Attrs;

import java.util.List;

/**
 * <p>
 * Middleware </p>
 *
 * @author ed
 */
public interface Middleware extends Attrs<Middleware> {

    public ExecutionContext handle(ExecutionContext ctx);

    public default void init(){

    }

    public Middleware next();

    /**
     * return the next
     */
    public Middleware next(Middleware ware);


    public default ExecutionContext exec(ExecutionContext ctx) {
        ExecutionContext result = this.handle(ctx);
        for (Middleware ware = this.next(); ware != null; ware = ware.next()) {
            result = ware.handle(result);
        }
        return result;
    }


    /**
     * string middlewares together as an sll
     *
     * @param wares
     * @return
     */
    public static Middleware string(List<Middleware> wares) {
        if (wares.size() == 0) {
            return null;
        }
        Middleware recent = wares.get(0);
        for (int i = 1; i < wares.size(); i++) {
            Middleware now = wares.get(i);
            if (recent != null) {
                recent.next(now);
            }
            recent = now;
        }
        return wares.get(0);
    }

}
