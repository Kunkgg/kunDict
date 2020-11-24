package kunDict;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class LongmanOnlineDict extends OnlineDict {

    public static final String name = "Longman Online English Dictionary";
    public static final String shortName = "longman";
    public static final String description = "English to English";
    public static final String queryUrlBase = "https://www.ldoceonline.com/dictionary/";
    public static final String homePage = "https://www.ldoceonline.com/";

    public LongmanOnlineDict() {
        super(name, shortName, description);
    }

    @Override
    public ArrayList<Word> queryWord(String wordSpell) {
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
}
