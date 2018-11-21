package pond.web.api.contract;

import pond.common.f.Callback;
import pond.web.router.Router;

import java.lang.annotation.Annotation;

public interface RouterContract<T extends Annotation> {

    Class<T> annotationType();

    Callback<Router> config();
}
