package pond.common.util.cui;

import org.junit.Test;
import pond.common.S;

/**
 * Created by ed on 7/14/15.
 */
public class TestRichLayout {
    @Test
    public static void main() {

        final int max = S.list.<Integer>one(new Integer[]{1, 1, 2, 3,
                4, 2, 5})
                .reduceLeft((a, b) -> S.math.max(a, b));
        S.echo(max);
        System.out.println(RichLayout.horizontal(new Rect(new String[] {
        "123123", "-----------", "sdsds" }), new Rect(new String[] {
        "123123sdsd", "-----------", "sdsdas", "23123", "sdas" }),
        new Rect(new String[] { "123123sdsd", "---xxxxsdsd",
        "sdasd56^&*(" })));
    }
}
