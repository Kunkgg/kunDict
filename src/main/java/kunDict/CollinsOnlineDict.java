package kunDict;


public class CollinsOnlineDict extends OnlineDict {

    public CollinsOnlineDict(){
        this.setName("Collins Online English Dictionary");
        this.setDescription("English to English");
        this.setType(DictType.Online);
    }

    private String queryUrlBase = "https://www.collinsdictionary.com/us/dictionary/english/";

    public Word query(String wordSpell) {
        String url = queryUrlBase + wordSpell;
        System.out.println("URL: " + url);

        Request req = new Request(url);
        Word word = null;
        try {
            String html = req.get().body();
            Extractor extractor = new Extractor(html);
            word = extractor.collinsOnline();

        } catch(NullPointerException e){
            e.printStackTrace();
            System.out.println("Http response is null.");
        }

        return word;
    };

    public String getQueryUrlBase() {
        return this.queryUrlBase;
    }
}
