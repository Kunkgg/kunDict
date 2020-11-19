package kunDict;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class LongmanOnlineDict extends OnlineDict {

    private static String name = "Longman Online English Dictionary";
    private static String shortName = "longman";
    private static String description = "English to English";
    private static String queryUrlBase = "https://www.ldoceonline.com/dictionary/";

    public LongmanOnlineDict() {
        super(name, shortName, description);
    }

    @Override
    public ArrayList<Word> queryWordBySpell(String wordSpell) {
        String url = queryUrlBase + preProcessWordSpell(wordSpell);
        Utils.debug("URL: " + url);
        Word word = null;
        ArrayList<Word> words = new ArrayList<>();

        try {
            Request req = new Request(url);
            String html = req.get().body();
            Extractor extractor = new Extractor(html);
            word = extractor.longmanOnline();
            if (word != null && ! word.isEmypty()) {
                words.add(word);
            }
        } catch (URISyntaxException e) {
            Utils.warning("Syntax error. Please check the spell of word.");
        }

        return words;
    };

    public static String getQueryUrlBase() {
        return queryUrlBase;
    }
}
