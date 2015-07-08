package pond.core.spi.server;

import pond.core.Pond;
import pond.core.spi.BaseServer;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractServer implements BaseServer{

    private Pond pond;
    private Map<String,Object> env = new HashMap<>();

    @Override
    public BaseServer env(String key, Object whatever) {
        env.put(key,whatever);
        return this;
    }

    @Override
    public Object env(String key) {
        return env.get(key);
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
