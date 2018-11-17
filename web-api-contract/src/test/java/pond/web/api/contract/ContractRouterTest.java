package pond.web.api.contract;

import org.junit.Test;
import pond.net.NetServer;
import pond.web.http.HttpConfigBuilder;

import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class ContractRouterTest {

    @Test
    public void of() throws InterruptedException, ExecutionException {
        Contract.Ignore ingore = new Annotation() {
            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        }
    }
}