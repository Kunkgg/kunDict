package kunDict;

import java.net.URISyntaxException;
import java.util.ArrayList;

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
    public ArrayList<Word> queryWordBySpell(String wordSpell) {
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
