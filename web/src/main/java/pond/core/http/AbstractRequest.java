package pond.core.http;

import pond.common.f.Callback;
import pond.core.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 15-7-23.
 */
public abstract class AbstractRequest implements Request {


    final protected Map<String, List<String>> headers = new HashMap<>();

    final protected Map<String, List<String>> params = new HashMap<>();

    final protected Map<String, List<UploadFile>> uploadFiles = new HashMap<>();

    final protected Map<String, Cookie> cookies = new HashMap<>();

    public AbstractRequest updateParams(Callback<Map<String, List<String>>> b) {
        b.apply(params);
        return this;
    }

    public AbstractRequest updateHeaders(Callback<Map<String, List<String>>> b) {
        b.apply(headers);
        return this;
    }

    public AbstractRequest updateUploadFiles(Callback<Map<String, List<UploadFile>>> b) {
        b.apply(uploadFiles);
        return this;
    }

    public AbstractRequest updateCookies(Callback<Map<String,Cookie>> b) {
        b.apply(cookies);
        return this;
    }


    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

    @Override
    public Map<String, List<String>> params() {
        return params;
    }

    @Override
    public Map<String, List<UploadFile>> files() {
        return uploadFiles;
    }

    @Override
    public Map<String, Cookie> cookies() {
        return cookies;
    }
}
