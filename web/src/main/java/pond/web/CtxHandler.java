package pond.web;

import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Callback;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 20/02/17.
 */
public interface CtxHandler extends Callback<Ctx> {

    CtxHandler NOOP = ctx -> {
    };

    CtxHandler[] EMPTY_ARRAY = new CtxHandler[0];




    static CtxHandler express(Mid m) {
        return ctx -> {
            if (ctx instanceof HttpCtx) {
                HttpCtx hctx = (HttpCtx) ctx;
                m.apply(hctx.req, hctx.resp);
            } else {
                throw new RuntimeException("can't Convert a non-http-web-ctx-handler to a middleware");
            }
        };
    }

    static void trustAllCertsWhenHttps (){
        try {
        TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};
        final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAll, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (s, sslSession) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    static CtxHandler proxy(String url) {
        return ctx -> {
            if (ctx instanceof HttpCtx) {
                HttpCtx hctx = (HttpCtx) ctx;

                HttpURLConnection connection = null;
                try {
                    String tail = (String) ctx.get("last_remainder");
                    if(tail.startsWith("/")) tail = tail.substring(1);
                    S.echo("uri ", hctx.route);
                    URL remote = new URL(url + tail);
                    connection = (HttpURLConnection) remote.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);


                    for(Map.Entry<String, List<String>> entry:  hctx.req.headers().entrySet()) {
                        connection.setRequestProperty(
                                entry.getKey(),
                                String.join(",", entry.getValue())
                        );
                    }
                    connection.setRequestMethod(hctx.method);
                    if(hctx.method.equalsIgnoreCase("PUT") || hctx.method.equalsIgnoreCase("POST")) {
                        STREAM.write(hctx.req.in(), connection.getOutputStream());
                    }
                    connection.connect();

                    for(Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                        for(String v : entry.getValue()) {
                            if(entry.getKey() != null){
                                System.out.println(entry.getKey() + ":" + v);

                                hctx.resp.header(entry.getKey(), v);
                            }
                        }
                    }
                    BufferedReader reader = null;
                    PrintWriter writer = hctx.resp.writer();
                    String tempLine = null;


                    hctx.flagToSendNormal();
                    STREAM.write(connection.getInputStream(), hctx.resp.out());
                    hctx.resp.status(connection.getResponseCode());

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    ctx.setHandled();
                }
            } else {
                throw new RuntimeException("can't Convert a non-http-web-ctx-handler to a middleware");
            }
        };
    }


}
