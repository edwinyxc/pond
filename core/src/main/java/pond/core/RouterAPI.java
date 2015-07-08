package pond.core;

import pond.common.f.Callback;
import pond.core.http.HttpMethod;

/**
 * Created by ed on 7/9/14.
 */
public interface RouterAPI {
    RouterAPI use(int mask, String path, Mid... mids);

    RouterAPI use(String path, Router router);

    default RouterAPI get(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.GET),
                path, mids);
    }


    default RouterAPI post(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.POST),
                path, mids);
    }

    default RouterAPI del(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.DELETE),
                path, mids);
    }

    default RouterAPI put(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.PUT),
                path, mids);
    }

}
