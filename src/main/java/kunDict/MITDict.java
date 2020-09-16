package kunDict;

public class MITDict extends Dict {

    final private String dbName = "mit_10k_dict";

    public MITDict(){
        this.setName("MIT 10K Englinsh Dictionary");
        this.setDescription("Word list is from MIT. Word entries are from Collins online dictionary.");
        this.setType(DictType.Locale);
    }

    public Word query(String wordSpell) {
        Word word = null;
        // query from locale database
        return word;
    };

    // public Boolean add(Word word) {
    // };
    // public Boolean delete(String wordSpell);
    // public Word random();
    // public Boolean update(Word word);
    // public int size();
    // public Boolean generate();
}
