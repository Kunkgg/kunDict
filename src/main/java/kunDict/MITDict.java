package kunDict;

import java.io.IOException;
import java.sql.SQLException;

public class MITDict extends LocalDict {

    public MITDict() throws IOException, SQLException{
        super("MIT Englinsh Dictionary",
            "Word list is from MIT. "
            + "Word entries are from Collins online dictionary.");
        this.setShortName("mit");
    }

    // public Word random();
    public void build() {

    };


}
