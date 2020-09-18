package kunDict;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class MITDict extends LocalDict {

    public MITDict() {
        super();
        this.setName("MIT 10K Englinsh Dictionary");
        this.setDescription(
                "Word list is from MIT. "
                +"Word entries are from Collins online dictionary.");
        this.setDbName("mit_10k_dict");
    }


    // public Boolean add(Word word) {
    // };
    // public Boolean delete(String wordSpell);
    // public Word random();
    // public Boolean update(Word word);
    // public int size();
    // public Boolean generate();


}
