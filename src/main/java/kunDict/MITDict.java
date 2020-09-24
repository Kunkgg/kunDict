package kunDict;

import java.io.IOException;
import java.sql.SQLException;

public class MITDict extends LocalDict {
    private static String name = "MIT Englinsh Dictionary";
    private static String shortName = "mit";
    private static String description = "Word list is from MIT. "
            + "Word entries are from Collins online dictionary.";

    public MITDict() throws IOException, SQLException{
        super(name, shortName, description);
    }

    // public Word random();
    public void build() {

    };


}
