package pond.common.s;

import pond.common.S;
import pond.common.util.logger.Logger;

/**
 * @author ed
 */
public class CacheTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        S.logger().config("default", Logger.DEBUG);
        S.logger().echo("ss");
        test();

    }

    public static void test() {
        S.logger().debug("ss");
    }

}
