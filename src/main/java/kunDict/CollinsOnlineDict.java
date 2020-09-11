package kunDict;

abstract class CollinsOnlineDict extends OnlineDict {

    public CollinsOnlineDict(String name, String description, DictType type) {
        super(name, description, type);
    }

    private String queryUrlBase = "https://www.collinsdictionary.com/us/dictionary/english/";

    public Word query(String wordSpell) {
        String url = queryUrlBase + wordSpell;

        Request req = new Request(url);
        String html = req.get().body();

    };

    public String getQueryUrlBase() {
        return this.queryUrlBase;
    }
}
