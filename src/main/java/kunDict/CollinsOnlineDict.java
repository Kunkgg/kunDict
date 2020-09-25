package kunDict;


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
        String url = queryUrlBase + wordSpell;
        Utils.debug("URL: " + url);

        Request req = new Request(url);
        Word word = null;
        try {
            String html = req.get().body();
            Extractor extractor = new Extractor(html);
            word = extractor.collinsOnline();

        } catch(NullPointerException e){
            e.printStackTrace();
            Utils.warning("Http response is null.");
        }

        return word;
    };
}
