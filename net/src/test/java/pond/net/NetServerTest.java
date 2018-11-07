package pond.net;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class NetServerTest {

    @Test
    public void test_server_start() throws InterruptedException, ExecutionException {
        new NetServer(ServerConfig.WELCOME.port(8080)).listen().get();
    }

}