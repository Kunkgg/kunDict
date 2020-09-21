package kunDict;

import java.io.IOException;
import java.sql.SQLException;

public class MITDict extends LocalDict {

    public MITDict() throws IOException, SQLException{
        super();
        this.setName("MIT 10K Englinsh Dictionary");
        this.setDescription(
                "Word list is from MIT. "
                +"Word entries are from Collins online dictionary.");
        this.setShortName("mit10k");
        Database db = new Database();
        this.setDb(db);
    }

    // public Boolean add(Word word) {
    // };
    // public Boolean delete(String wordSpell);
    // public Word random();
    // public Boolean update(Word word);
    // public int size();
    // public Boolean generate();


}
