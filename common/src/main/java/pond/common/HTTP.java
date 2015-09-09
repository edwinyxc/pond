package pond.common;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import pond.common.f.Callback;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by ed on 7/14/15.
 * //TODO test
 */
public class HTTP {

  public static void get(String uri) throws IOException {
    get(uri, null, Callback.noop());
  }

  public static void get(String uri, Callback<HttpResponse> cb) throws IOException {
    get(uri, null, cb);
  }

  public static void get(String uri, Map<String, String> headers) throws IOException {
    get(uri, headers, Callback.noop());
  }

  public static void get(String uri, Map<String, String> headers, Callback<HttpResponse> cb) throws IOException {
    HttpGet get = new HttpGet(uri);

    S._for(S.avoidNull(headers, Collections.<String, Object>emptyMap())
               .entrySet()).each(e -> get.addHeader(e.getKey(), String.valueOf(e.getValue())));

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(get);
    ) {
      cb.apply(resp);
    }
  }


  public static void put(String uri) throws IOException {
    put(uri, null, null, Callback.noop());
  }

  public static void put(String uri, Callback<HttpResponse> cb) throws IOException {
    put(uri, null, null, cb);
  }

  public static void put(String uri, Map<String, Object> params) throws IOException {
    put(uri, params, null, Callback.noop());
  }

  public static void put(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    put(uri, params, null, cb);
  }

  public static void put(String uri, Map<String, Object> params, Map<String, String> headers, Callback<HttpResponse> cb) throws IOException {

    HttpPut post = new HttpPut(uri);
    HttpEntity form = new UrlEncodedFormEntity(
        S._tap(S.array(),
               arr -> arr.addAll(
                   S._for(S.avoidNull(params, Collections.<String, Object>emptyMap()).entrySet())
                       .map(e -> new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue()))).toList()
               )
        ), Consts.UTF_8
    );

    S._for(S.avoidNull(headers, Collections.<String, Object>emptyMap())
               .entrySet()).each(e -> post.addHeader(e.getKey(), String.valueOf(e.getValue())));

    post.setEntity(form);

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(post);
    ) {
      cb.apply(resp);
    }
  }

  public static void delete(String uri) throws IOException {
    delete(uri, null, Callback.noop());
  }

  public static void delete(String uri, Callback<HttpResponse> cb) throws IOException {
    delete(uri, null, cb);
  }

  public static void delete(String uri, Map<String, String> headers) throws IOException {
    delete(uri, headers, Callback.noop());
  }

  public static void delete(String uri, Map<String, String> headers, Callback<HttpResponse> cb) throws IOException {
    HttpDelete delete = new HttpDelete(uri);

    S._for(S.avoidNull(headers, Collections.<String, Object>emptyMap())
               .entrySet()).each(e -> delete.addHeader(e.getKey(), String.valueOf(e.getValue())));

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(delete);
    ) {
      cb.apply(resp);
    }
  }

  public static void post(String uri) throws IOException {
    post(uri, null, null, Callback.noop());
  }

  public static void post(String uri, Callback<HttpResponse> cb) throws IOException {
    post(uri, null, null, cb);
  }

  public static void post(String uri, Map<String, Object> params) throws IOException {
    post(uri, params, null, Callback.noop());
  }

  public static void post(String uri, Map<String, Object> params, Callback<HttpResponse> cb) throws IOException {
    post(uri, params, null, cb);
  }

  public static void post(String uri, Map<String, Object> params, Map<String, String> headers) throws IOException {
    post(uri, params, headers, Callback.noop());
  }

  public static void post(String uri,
                          Map<String, Object> params,
                          Map<String, String> headers,
                          Callback<HttpResponse> cb) throws IOException {
    HttpPost post = new HttpPost(uri);
    HttpEntity form = new UrlEncodedFormEntity(
        S._tap(S.array(),
               arr -> arr.addAll(
                   S._for(S.avoidNull(params, Collections.<String, Object>emptyMap()).entrySet())
                       .map(e -> new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue()))).toList()
               )
        ), Consts.UTF_8
    );

    S._for(S.avoidNull(headers, Collections.<String, Object>emptyMap())
               .entrySet()).each(e -> post.addHeader(e.getKey(), String.valueOf(e.getValue())));

    post.setEntity(form);

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(post);
    ) {
      cb.apply(resp);
    }

  }

  public static void postMultipart(String uri,
                                   Map<String, Object> params,
                                   Map<String, File> uploads,
                                   Map<String, String> headers,
                                   Callback<HttpResponse> cb) throws IOException {

    HttpPost post = new HttpPost(uri);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    S._for(S.avoidNull(params, Collections.<String, Object>emptyMap()).entrySet()).each(e -> {
      String name = e.getKey();
      String value = String.valueOf(e.getValue());

      builder.addTextBody(name, value, ContentType.TEXT_HTML); //default is text/plain
    });


    S._for(S.avoidNull(uploads, Collections.<String, File>emptyMap())).each(e -> {
      String name = e.getKey();
      File f = e.getValue();

      builder.addBinaryBody(name, f, ContentType.APPLICATION_OCTET_STREAM, f.getName());
    });

    S._for(S.avoidNull(headers, Collections.<String, Object>emptyMap())
               .entrySet()).each(e -> post.addHeader(e.getKey(), String.valueOf(e.getValue())));

    post.setEntity(builder.build());

    //post.setHeader("Content-Type", "multipart/form-data");

    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse resp = client.execute(post)
    ) {
      cb.apply(resp);
    }

  }

}

