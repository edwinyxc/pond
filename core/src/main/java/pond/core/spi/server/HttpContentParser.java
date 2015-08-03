package pond.core.spi.server;


import pond.common.f.Callback;
import pond.core.http.AbstractRequest;

public class HttpContentParser{
    final String contentType;
    final Callback<AbstractRequest> callback;

    public HttpContentParser(String contentType, Callback<AbstractRequest> callback){
        this.contentType = contentType.toLowerCase();
        this.callback = callback;
    }

    public void parse(AbstractRequest request){
        callback.apply(request);
    }

}