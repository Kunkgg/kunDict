package kunDict;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * Request
 */
public class Request {
    private String url;

    public Request(String url){
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
        String html = "";
        try {
            URL url = new URL(this.url);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream())
                    );

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            html += inputLine;
        }
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("HTTP request failed!");
        }

        return html;
    }

    public String post() {
        return "html";
    }
}
