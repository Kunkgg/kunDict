package kunDict;

public class SQLStr {
    // static fields {{{ //
    static String[] tableListInDict = {"words", "frequencies",
        "entries", "examples" };

    static String[] columnListInDict = {"word_spell",
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
    // }}} static fields //

    public static String commaJoin(String... columns) {
        return String.join(", ", columns);
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


    // Create table for each dictionary {{{ //
    public static String hasTables(String shortName) {
        return "SHOW TABLES LIKE \'%" + shortName + "%\';";
    }

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
                  + "PRIMARY KEY(example_id),"
                  + "FULLTEXT(example_text)"
                + ") ENGINE=MyISAM;";
    }

    // }}} Create table for each dictionary //
}
