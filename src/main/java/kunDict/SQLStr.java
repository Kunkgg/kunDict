package kunDict;

public class SQLStr {
    // static fields {{{ //
    static final String[] tableListInDict = {"words", "frequencies",
        "entries", "examples" };

    static final String[] columnListInDict = {"word_spell",
                            "word_source",
                            "word_forms",
                            "word_pron_soundmark",
                            "word_pron_sound",
                            "word_counter",
                            "word_timestamp",
                            "fre_band",
                            "fre_description",
                            "entry_wordClass",
                            "entry_sense",
                            "example_text"};

    static final String[] tableListApp = {"dicts", "dict_types"};
    // }}} static fields //

    public static String commaJoin(String... columns) {
        return String.join(", ", columns);
    }

    public static String hasTables(String keyword) {
        return "SHOW TABLES LIKE \'%" + keyword + "%\';";
    }

    // operate word in a dictionary {{{ //
    public static String queryWord(String shortName, String wordSpell) {
        String columns = commaJoin(columnListInDict);

        String tables = tableStrDictQueryWord(shortName, tableListInDict);
        String wheres = whereStrDictQueryWord(shortName, wordSpell);
        String query = "SELECT " + columns + " FROM " + tables
            + " WHERE (" + wheres + ")";

        return query;
    }

    public static String tableStrDictQueryWord(String shortName,
            String... tables) {

        for (int i = 0; i < tables.length; i++) {
            tables[i] = shortName + "_" + tables[i];
        }
        return String.join(", ", tables);
    }

    public static String joinConditionStrDict(String shortName, String table1, String table2, String foreignKey)
    {
        return shortName + "_" + table1 + "." + foreignKey
            + " = " + shortName + "_" + table2 + "." + foreignKey;
    }

    public static String whereStrDictQueryWord(String shortName,
            String wordSpell) {
        String[] conditions = new String[4];
        conditions[0] = joinConditionStrDict(shortName,
                            "words", "frequencies", "fre_id");
        conditions[1] = joinConditionStrDict(shortName,
                            "words", "entries", "word_id");
        conditions[2] = joinConditionStrDict(shortName,
                            "entries", "examples", "entry_id");
        conditions[3] = shortName + "_words.word_spell = " + "\'"
            + wordSpell + "\'";

        return String.join(" AND ", conditions);
    }
    // }}} operate word in a dictionary //

    // Create table in a dictionary {{{ //
    public static String createTableWords(String shortName) {
        return "CREATE TABLE " + shortName + "_words"
            + "("
              + "word_id                int       NOT NULL AUTO_INCREMENT,"
              + "word_spell             char(50)  NOT NULL UNIQUE,"
              + "word_source            char(50)  NULL ,"
              + "word_forms             char(255) NULL ,"
              + "word_pron_soundmark    char(50)  NULL ,"
              + "word_pron_sound        char(255) NULL ,"
              + "fre_id                 int       NULL ,"
              + "word_counter           int       DEFAULT 0,"
              + "word_timestamp         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + "PRIMARY KEY (word_id)"
            + ") ENGINE=InnoDB;";
    }

    public static String createTableFrequencies(String shortName) {
        return "CREATE TABLE " + shortName + "_frequencies"
                + "("
                  + "fre_id             int       NOT NULL AUTO_INCREMENT,"
                  + "fre_band           int       NOT NULL UNIQUE,"
                  + "fre_description    text      NULL ,"
                  + "PRIMARY KEY(fre_id)"
                + ") ENGINE=InnoDB;";
    }

    public static String createTableEntries(String shortName) {
        return "CREATE TABLE " + shortName + "_entries"
                + "("
                  + "entry_id         int       NOT NULL AUTO_INCREMENT,"
                  + "entry_wordClass  char(255) NOT NULL ,"
                  + "entry_sense      text      NULL ,"
                  + "word_id          int       NULL ,"
                  + "PRIMARY KEY(entry_id)"
                + ") ENGINE=InnoDB;";
    }

    public static String createTableExamples(String shortName) {
        return "CREATE TABLE " + shortName + "_examples"
                + "("
                  + "example_id      int       NOT NULL AUTO_INCREMENT,"
                  + "example_text    text      NOT NULL ,"
                  + "entry_id        int       NOT NULL ,"
                  + "PRIMARY KEY(example_id)"
                + ") ENGINE=InnoDB;";
    }

    public static String addForeignKeyFreId(String shortName) {
        return addForeignKey(shortName, "words", "frequencies", "fre_id");
    }

    public static String addForeignKeyWordId(String shortName) {
        return addForeignKey(shortName, "entries", "words", "word_id");
    }

    public static String addForeignKeyEntryId(String shortName) {
        return addForeignKey(shortName, "examples", "entries", "entry_id");
    }

    public static String addForeignKey(String shortName,
            String tableA, String tableB, String foreignKey) {
        String constraintName = shortName + "_fk_" + tableA + "_" + tableB;
        tableA = shortName + "_" + tableA;
        tableB = shortName + "_" + tableB;

        return "ALTER TABLE " + tableA
            + " ADD CONSTRAINT " + constraintName
            + " FOREIGN KEY (" + foreignKey + ")"
            + " REFERENCES " + tableB + "(" + foreignKey + ");";
    }
    // }}} Create table in a dictionary //

    // Delete tables for each dictionary {{{ //
    public static String dropTableInDict(String shortName) {
        String[] tableListInDictWithShortName = new String[tableListInDict.length];
        for (int i = 0; i < tableListInDictWithShortName.length; i++) {
            tableListInDictWithShortName[i] = shortName + "_"
                + tableListInDict[i];
        }

        return "DROP TABLE " + commaJoin(tableListInDictWithShortName) + ";";
    }
    // }}} Delete tables for each dictionary //

    // Create tables for App {{{ //
    public static String createTableDicts() {
        return "CREATE TABLE dicts"
            + "("
            + "  dict_id                int       NOT NULL AUTO_INCREMENT,"
            + "  dict_name              char(50)  NOT NULL UNIQUE,"
            + "  dict_type_id           int       NULL ,"
            + "  dict_dbName            char(50)  NULL UNIQUE,"
            + "  dict_size              int       DEFAULT 0,"
            + "  dict_timestamp         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
            + "  PRIMARY KEY (dict_id)"
            + ") ENGINE=InnoDB;";
    }

    public static String createTableDictTypes() {
        return "CREATE TABLE dict_types"
            + "("
            + "  dict_type_id           int       NOT NULL AUTO_INCREMENT,"
            + "  dict_type              char(50)  NULL UNIQUE,"
            + "  PRIMARY KEY (dict_type_id)"
            + ") ENGINE=InnoDB;";
    }

    public static String addForeignKeyDictTypeId() {
        return "ALTER TABLE dicts ADD CONSTRAINT fk_dicts_types "
            + "FOREIGN KEY (dict_type_id) "
            + "REFERENCES  dict_types (dict_type_id);";
    }

    // }}} Create tables for App //
}
