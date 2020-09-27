package kunDict;

import java.net.URISyntaxException;

public class CollinsOnlineDict extends OnlineDict {

    private static String name = "Collins Online English Dictionary";
    private static String shortName = "collins";
    private static String description = "English to English";
    private static String queryUrlBase = "https://www.collinsdictionary.com/"
                                    + "us/dictionary/english/";

    public CollinsOnlineDict(){
        super(name, shortName, description);
    }

    @Override
    public Word queryWord(String wordSpell) {
        String url = queryUrlBase + preProcessWordSpell(wordSpell);
        Utils.debug("URL: " + url);
        Word word = null;

        try {
            Request req = new Request(url);
            String html = req.get().body();
            Extractor extractor = new Extractor(html);
            word = extractor.collinsOnline();
        } catch (URISyntaxException e) {
            Utils.warning("Syntax error. Please check the spell of word.");
        }

        return word;
    };

}
