package pond.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PATHTest {


    @Test
    public void testGet() throws Exception {
        assertEquals(PATH.classpathRoot(), S.class.getResource("/").getPath());
    }


    @Test
    public void testDetectWebRootPath() throws Exception {
        S.echo(PATH.detectWebRootPath());
    }

    @Test
    public void testIsAbsolute() throws Exception {

    }

}