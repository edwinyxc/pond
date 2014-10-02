package pond.db;

import pond.common.S;
import pond.common.sql.Criterion;

import java.util.List;

import static pond.common.S._for;

/**
 * Created by ed on 9/29/14.
 */
public class ModelTest {

    public static void test2() {
        List<Model> list =  TestModel.dao().query();
        S.echo(S.dump(_for(list).map(Model::view).toList()));
    }


    public static void main(String[] args){
        test2();
//        test3();
//        test4();
//        test5();
    }
}
