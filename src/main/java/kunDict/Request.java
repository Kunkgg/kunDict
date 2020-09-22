package kunDict;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import java.lang.InterruptedException;

/**
 * Request
 */
public class Request {
    private static String proxyHost = "127.0.0.1";
    private static int proxyPort = 1081;
    HttpClient client;
    HttpRequest.Builder requestBuilder;
    HttpResponse.BodyHandler bodyHandler;
    String url;

    public Request(String url) {
        this.url = url;
        this.client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(
                        new InetSocketAddress(proxyHost, proxyPort)))
            .build();
        this.requestBuilder = HttpRequest.newBuilder(URI.create(url));
        this.bodyHandler = BodyHandlers.ofString();
    }

    public HttpResponse<String> get() {
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, bodyHandler);
            Utils.info("status code: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Utils.warning("Http request failed.");
        }

        return response;
    }
}
