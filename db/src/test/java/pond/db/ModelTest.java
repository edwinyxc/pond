package pond.db;

import pond.common.S;
import pond.common.sql.Criterion;

import java.util.List;

import static pond.common.S._for;
import static pond.db.Model.*;

import org.junit.Test;
/**
 * Created by ed on 9/29/14.
 */
public class ModelTest {

    public static void test2() {
        List<Model> list = dao(TestModel.class).query();
        S.echo(S.dump(_for(list).map(Model::view).toList()));
    }


    //@Test
    public void test() {
        test2();
    }
//        test3();
//        test4();
//        test5();
}
