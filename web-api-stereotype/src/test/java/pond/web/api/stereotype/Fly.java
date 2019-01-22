package pond.web.api.stereotype;

import pond.net.NetServer;
import pond.web.http.HttpConfigBuilder;

import java.util.concurrent.ExecutionException;

public class Fly {

    public static void main(String args[]) throws InterruptedException, ExecutionException {
        new NetServer(new HttpConfigBuilder().handler(
            ContractRouter.of(new MyDummy())
        )).listen(9090).get();
    }
}
