package kunDict;

/**
 * Url
 */
public class Url {
    private String url;

    public Url(String url){
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public String setUrl(String url) {
        this.url = url;
        return this.url;
    }

    public String get() {
        return "html";
    }

    public String post() {
        return "html";
    }
}
