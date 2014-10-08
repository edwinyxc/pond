package pond.core;

import pond.common.f.Callback;
import pond.core.http.HttpMethod;
import pond.core.router.Router;

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

    default RouterAPI get(String path, Callback.C2<Request, Response> mid) {
        return use(HttpMethod.mask(HttpMethod.GET),
                path, (req, resp, next) -> mid.apply(req, resp));
    }

    default RouterAPI post(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.POST),
                path, mids);
    }

    default RouterAPI post(String path, Callback.C2<Request, Response> mid) {
        return use(HttpMethod.mask(HttpMethod.POST),
                path, (req, resp, next) -> mid.apply(req, resp));
    }

    default RouterAPI del(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.DELETE),
                path, mids);
    }

    default RouterAPI del(String path, Callback.C2<Request, Response> mid) {
        return use(HttpMethod.mask(HttpMethod.DELETE),
                path, (req, resp, next) -> mid.apply(req, resp));
    }

    default RouterAPI put(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.PUT),
                path, mids);
    }

    default RouterAPI put(String path, Callback.C2<Request, Response> mid) {
        return use(HttpMethod.mask(HttpMethod.PUT),
                path, (req, resp, next) -> mid.apply(req, resp));
    }
}
