package pond.common;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ed on 15-7-1.
 */
public class DebugModeTest {
    final Logger logger1 = LoggerFactory.getLogger(DebugModeTest.class);

    @Test
    public void test(){
        S._debug_on(DebugModeTest.class);
        S._debug(logger1, log -> {
            Assert.assertTrue(true);
            S.echo("333");
            log.info("i'm here");
        });

        S._debug_off(DebugModeTest.class);
        S._debug(logger1, log -> {
            Assert.assertTrue(false);
            log.info("i mustn't be here");
        });
    }

}
