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
import java.net.http.HttpTimeoutException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

/**
 * Request
 */
public class Request {
    private String proxyHost;
    private int proxyPort = -1;
    private boolean redirect = true;
    private int maxRedirect = 5;
    private int redirectCounter = 0;
    private Duration timeout = Duration.ofSeconds(10);

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

    public Request(String url) throws URISyntaxException {
        this.loadConfigs();
        this.url = url;
        this.initializeClient();
        this.setUrlIntoRequestBuilder();
        this.requestBuilder.timeout(this.timeout);
        this.bodyHandler = BodyHandlers.ofString();
    }

    public void loadConfigs() {
        this.proxyHost = App.configs.getProperty("requestProxyHost");
        if (!App.configs.getProperty("requestProxyPort").equals("")) {
            this.proxyPort = Integer.parseInt(App.configs.getProperty("requestProxyPort"));
        }
        this.timeout = Duration.ofSeconds(
                Integer.parseInt(App.configs.getProperty("requestTimeout")));
        this.redirect = Utils.testString(
                App.configs.getProperty("requestRedirect"));
        this.maxRedirect = Integer.parseInt(
                App.configs.getProperty("requestMaxRedirect"));
    }

    public void initializeClient() {
        if (this.proxyHost == null || this.proxyHost.equals("")
                || this.proxyPort == -1) {
            this.client = HttpClient.newBuilder().build();
            Utils.info("Http request without proxy");
        } else {
            this.client = HttpClient.newBuilder()
                    .proxy(ProxySelector
                            .of(new InetSocketAddress(proxyHost, proxyPort)))
                    .build();
            Utils.info(String.format("Http request with proxy: %s:%d",
                        this.proxyHost, this.proxyPort));
        }
    }

    public void setUrlIntoRequestBuilder() throws URISyntaxException {
        this.requestBuilder = HttpRequest.newBuilder(URI.create(this.url));
    };

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
        } catch (HttpTimeoutException e) {
            Utils.warning(String.format("Http request timeout(%ds).", this.timeout.toSecondsPart()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Utils.warning("Http request failed.");
        } catch(URISyntaxException e) {
            Utils.warning("Syntax error. Please check the spell of word.");
        }


        return response;
    }
}
