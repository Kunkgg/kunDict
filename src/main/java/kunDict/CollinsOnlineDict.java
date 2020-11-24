package kunDict;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class CollinsOnlineDict extends OnlineDict {

    public static final String name = "Collins Online English Dictionary";
    public static final String shortName = "collins";
    public static final String description = "English to English";
    public static final String queryUrlBase = "https://www.collinsdictionary.com/"
                                    + "us/dictionary/english/";

    public CollinsOnlineDict(){
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
            word = extractor.collinsOnline();
            if (word != null && ! word.isEmypty()) {
                words.add(word);
            }
        } catch (URISyntaxException e) {
            Utils.warning("Syntax error. Please check the spell of word.");
        }

        return words;
    };

}
