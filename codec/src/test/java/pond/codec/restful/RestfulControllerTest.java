package pond.codec.restful;

import pond.codec.RestfulController;
import pond.core.Pond;

public class RestfulControllerTest {
    public static void main(String[] args){
        Pond app = Pond.init().debug();
        app.use("/users",new RestfulController<>(new TestRecord()));
        app.listen(8080);
    }

}