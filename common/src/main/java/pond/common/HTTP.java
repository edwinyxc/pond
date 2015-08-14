package pond.common;

import org.apache.http.Consts;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import pond.common.f.Callback;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 7/14/15.
 * //TODO test
 */
public class HTTP {
  public static void get(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    send(new HttpGet(uri), params, cb);
  }

  public static void post(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    send(new HttpPost(uri), params, cb);
  }

  public static void put(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    send(new HttpPut(uri), params, cb);
  }

  public static void delete(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    send(new HttpDelete(uri), params, cb);
  }

  public static void send(HttpUriRequest request, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    if (params == null) params = Collections.emptyMap();
    List<NameValuePair> dummyform = new ArrayList<>();
    S._for(params).each((e) -> {
      dummyform.add(new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())));
    });

    if (request instanceof HttpPost ||
        request instanceof HttpPut) {
      ((HttpEntityEnclosingRequest) request).setEntity(new UrlEncodedFormEntity(dummyform, Consts.UTF_8));
    } else {
      String uri = request.getURI().toString();
      String query = URLEncodedUtils.format(dummyform, Consts.UTF_8);
      if (STRING.notBlank(query)) {
        uri += "?" + query;
      }
      ((HttpRequestBase) request).setURI(URI.create(uri));
    }

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(request);
    ) {
      cb.apply(resp);
    }
  }
}
