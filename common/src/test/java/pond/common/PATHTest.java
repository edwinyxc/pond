package pond.common;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class PATHTest {



    @Test
    public void testGet() throws Exception {

        S.echo(PATH.class.getResource("").getPath());;
        S.echo(PATH.class.getResource("/").getPath());;
        S.echo(PATH.class.getResource("/").getPath());;
        S.echo(Integer.class.getResource("/").getPath());
        S.echo(Integer.class.getResource("").getPath());
        S.echo(String.class.getResource("").getPath());

    }


    @Test
    public void testRootClassPath() throws Exception {

    }

    @Test
    public void testDetectWebRootPath() throws Exception {

    }

    @Test
    public void testIsAbsolute() throws Exception {

    }
}