package pond.core.spi.server;

import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.Pond;
import pond.core.Request;
import pond.core.Response;
import pond.core.spi.BaseServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public abstract class AbstractServer implements BaseServer {

    private Pond pond;
    private Callback.C2<Request, Response> handler;
    private Function<Object, String> envGetter = System::getProperty;

    protected Callback.C2<Request, Response> handler(){
        return this.handler;
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

}
