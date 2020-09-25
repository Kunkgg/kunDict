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
    private boolean redirect = true;
    private int maxRedirect = 5;
    private int redirectCounter = 0;

    private HttpClient client;
    private HttpRequest.Builder requestBuilder;
    private HttpResponse.BodyHandler bodyHandler;
    private String url;

    // setter {{{ //
    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public void setMaxRedirect(int maxRedirect) {
        this.maxRedirect = maxRedirect;
    }

    public void setRedirectCounter(int redirectCounter) {
        this.redirectCounter = redirectCounter;
    }

    public void setClient(HttpClient client){
        this.client = client;
    }

    public void setRequestBuilder(HttpRequest.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public void setBodyHandler(HttpResponse.BodyHandler bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    // }}} setter //

    // getter {{{ //
    public boolean getRedirect() {
        return this.redirect;
    }

    public int getMaxRedirect() {
        return this.maxRedirect;
    }

    public int getRedirectCounter() {
        return this.redirectCounter;
    }

    public HttpClient getClient() {
        return this.client;
    }

    public HttpRequest.Builder getRequestBuilder() {
        return this.requestBuilder;
    }

    public HttpResponse.BodyHandler getBodyHandler() {
        return this.bodyHandler;
    }

    public String getUrl() {
        return this.url;
    }

    // }}} getter //

    public Request(String url) {
        this.url = url;
        this.client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(
                        new InetSocketAddress(proxyHost, proxyPort)))
            .build();
        this.setUrlIntoRequestBuilder();
        this.bodyHandler = BodyHandlers.ofString();
    }

    public void setUrlIntoRequestBuilder(){
        this.requestBuilder = HttpRequest.newBuilder(URI.create(this.url));
    }

    public HttpResponse<String> get() {
        HttpResponse<String> response = null;
        HttpRequest request = this.requestBuilder.build();

        try {
            response = client.send(request, bodyHandler);
            int statusCode = response.statusCode();
            Utils.debug("status code: " + statusCode);
            if (this.redirect && 300 <= statusCode && statusCode < 400
                    && this.redirectCounter < this.maxRedirect) {
                this.redirectCounter++;
                HttpHeaders headers = response.headers();
                Optional<String> location = headers.firstValue("location");
                Utils.debug("redirect count: " + this.redirectCounter);
                Utils.debug("redirect location: " + location.get());

                this.url = location.get();
                this.setUrlIntoRequestBuilder();
                response = this.get();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Utils.warning("Http request failed.");
        }

        return response;
    }
}
