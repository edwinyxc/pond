package pond.web.api.stereotype;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutionException;

public class ContractRouterTest {

    @Test
    public void of() throws InterruptedException, ExecutionException {
        Contract.Ignore ingore = new Contract.Ignore() {
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
        };
    }
}