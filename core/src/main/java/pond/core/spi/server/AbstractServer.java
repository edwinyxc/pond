package pond.core.spi.server;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.Pond;
import pond.core.Request;
import pond.core.Response;
import pond.core.spi.BaseServer;

import java.util.concurrent.*;

public abstract class AbstractServer implements BaseServer{

    private Pond pond;
    private Callback.C2<Request, Response> handler;
    private Function<Object,String> envGetter = System::getProperty;

    protected ExecutorService executor;
    protected Runnable actor(Request req, Response resp){
        return () ->{
            S.echo("beginnnnnn");
            S.echo(this.handler);
            this.handler.apply(req, resp);
        };
    }

    @Override
    public void regEnv(Function<Object, String> f) {
        envGetter = f;
    }

    @Override
    public Object env(String key) {
        return envGetter.apply(key);
    }

    @Override
    public void pond(Pond pond) {
        this.pond = pond;
    }

    @Override
    public Pond pond() {
        return pond;
    }

    @Override
    public void handler(Callback.C2<Request, Response> handler) {
        this.handler = handler;
    }

    @Override
    public void executor(ExecutorService executor) {
        this.executor = executor;
    }
}
