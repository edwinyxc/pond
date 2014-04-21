package com.shuimin.jtiny.core.server;

import com.shuimin.jtiny.core.*;
import com.shuimin.jtiny.core.exception.HttpException;
import com.shuimin.jtiny.core.exception.YException;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by ed on 2014/4/11.
 */
public abstract class AbstractServer implements Server {
    private List<Middleware> execChain = new LinkedList<>();

    @Override
    public Server use(Middleware handler) {
        execChain.add(handler);
        return this;
    }

    protected final RequestHandler chainedHandler = (req, resp) -> {

        try {
            ExecutionContext ctx = ExecutionContext.init(req, resp);
            ExecutionManager.ExecutionContexts.set(ctx);
            for (int i = 0; i < execChain.size(); i++) {
                Middleware m = execChain.get(i);
                try {
                    m.handle(ctx);
                } catch (Interrupt.JumpInterruption jump) {
                    //continue;
                } catch (Interrupt.KillInterruption kill) {
                    break;
                } catch (Interrupt.RedirectInterruption redirection) {
                    ctx.resp().redirect(redirection.uri());
                    return;
                } catch (Interrupt.RenderInterruption render) {
                    render.value().render(ctx.resp());
                    return;
                } catch (HttpException e) {
                    //not report until it be the last 404
                    if (e.code() == 404 && i != execChain.size() - 1) {
                        //continue;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (HttpException e) {
            resp.sendError(e.code(), e.getMessage());
        } catch (YException e) {
            resp.sendError(500, e.toString());
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            ExecutionManager.ExecutionContexts.remove();
        }
    };
}
