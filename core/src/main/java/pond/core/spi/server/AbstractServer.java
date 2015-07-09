package pond.core.spi.server;

import pond.common.f.Function;
import pond.core.Pond;
import pond.core.spi.BaseServer;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractServer implements BaseServer{

    private Pond pond;

    private Function<Object,String> envGetter = System::getProperty;
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
}
