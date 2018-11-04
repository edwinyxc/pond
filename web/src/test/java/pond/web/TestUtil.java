package pond.web;

import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STREAM;
import pond.web.http.HttpConfigBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

/**
 * Created by ed on 8/24/15.
 */
public class TestUtil {
    public static final HttpClient client = HttpClient.newHttpClient();

    public static CompletableFuture<String> GET_BODY_AS_STRING(String url) throws IOException {

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                        .GET()
                        .uri(new URI(url))
                        .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return client
				.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body);
    }


}
