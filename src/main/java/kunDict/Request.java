package kunDict;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

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

    public HttpResponse<String> get(boolean redirect) {
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, bodyHandler);
            int statusCode = response.statusCode();
            Utils.debug("status code: " + statusCode);
            if (redirect && 300 <= statusCode && statusCode < 400) {
                HttpHeaders headers = response.headers();
                Optional<String> location = headers.firstValue("location");
                Utils.debug("redirect location: " + location.get());

                Request redirectRequest = new Request(location.get());
                response = redirectRequest.get(true);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Utils.warning("Http request failed.");
        }

        return response;
    }
}
