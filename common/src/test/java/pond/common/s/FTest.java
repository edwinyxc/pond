package pond.common.s;

import pond.common.S;
import pond.common.f.None;
import pond.common.f.Tuple;

/**
 * @author ed
 */
public class FTest {
    static Tuple<String, Integer> test() {
        return Tuple.t2("a", 2);
    }

    static None avoidNull() {
        return S._none();
    }

    static void test_none() {
        S.echo(avoidNull() == null);
        S.echo(avoidNull().isPresent());
        S.echo(avoidNull());
    }

    public static void main(String[] args) {
        S.echo(test()._a);
        S.echo(test()._b);
        test_none();
    }
}
