package com.shuimin.pond.codec.restful;

import com.shuimin.pond.core.Pond;

import static org.junit.Assert.*;

public class RestfulControllerTest {
    public static void main(String[] args){
        Pond app = Pond.init().debug();
        app.use("/users",new RestfulController<>(new TestRecord()));
        app.listen(8080);
    }

}