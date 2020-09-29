package kunDict;

import java.io.IOException;
import java.sql.SQLException;

public class DefaultLocalDict extends LocalDict {
    private static String name = "Default local dictionary";
    private static String shortName = "def";
    private static String description = "the default local dictionary";

    public DefaultLocalDict() throws SQLException{
        super(name, shortName, description);
        this.initializeTables();
    }

    public void build() {

    };
}
