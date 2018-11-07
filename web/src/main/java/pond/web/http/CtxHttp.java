package pond.web.http;

import pond.net.CtxNet;
import pond.web.Request;

import java.util.List;
import java.util.Map;

public interface CtxHttp extends CtxNet {
    enum Keys {
        Builder("Builder"),
        NettyRequest("NettyRequest"),
        NettyResponse("NettyResponse"),
        Request("Request"),
        Response("Response"),
        In("In"),
        Out("Out"),
        Headers("Headers"),
        Queries("Queries"),
        InUrlParams("InUrlParams"),
        FormData("FormData"),
        Cookies("Cookies"),
        UploadFiles("UploadFiles");
        final String _name;

        Keys(String name) {
            _name = CtxHttp.class.getName() + "." + name;
        }
    }

    default Request req() {
        return (Request) this.properties().get(Keys.Request._name);
    }

    default Request resp() {
        return (Request) this.properties().get(Keys.Response._name);
    }

    default CtxHttpBuilder builder() {
        return (CtxHttpBuilder) this.properties().get(Keys.Builder._name);
    }

    interface Cookie extends CtxHttp {

    }

    interface FormData extends CtxHttp {

    }

    interface UploadFiles extends CtxHttp {
        @SuppressWarnings("unchecked")
        default Map<String, List<Request.UploadFile>> files() {
            return (Map<String, List<Request.UploadFile>>) this.properties().get(Keys.UploadFiles._name);
        }
    }

    interface In extends CtxHttp {

    }

    interface Out extends CtxHttp {

    }

}

