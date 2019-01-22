package pond.web.api.stereotype;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public class ResponsesObject {
    Map<HttpResponseStatus, ResponseObject> _innerMap;
}
