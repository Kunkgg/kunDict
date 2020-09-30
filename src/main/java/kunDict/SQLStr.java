package kunDict;

import java.util.ArrayList;

public class SQLStr {
    // SQLException code {{{ //
    static final int ERRORCODE_DUPLICATE_ENTRY = 1062;
    // X0Y32: Jar file already exists in schema
    static final String SQLSTATE_JAR_FILE_EXISTED = "X0Y32";
    // 42Y55: Table already exists in schema
    static final String SQLSTATE_TABLE_EXISTED = "X0Y32";

    // }}} SQLException code //
    // static fields {{{ //
    static final String[] tableListInDict = { "words", "frequencies",
            "entries", "examples" };

    static final String[] columnListInQueryWord = { "word_spell", "word_source",
            "word_forms", "word_pron_soundmark", "word_pron_sound",
            "word_acounter", "word_mtime", "fre_band", "fre_description",
            "entry_wordClass", "entry_sense", "example_text", "word_atime"};

    static final String[] columnListInFrequencies = { "fre_band",
            "fre_description" };

    static final String[] columnListInWords = { "word_spell", "word_source",
            "word_forms", "word_pron_soundmark", "word_pron_sound",
            "word_atime", "word_acounter", "word_mtime","fre_id"};
    static final String[] columnListInEntries = { "entry_wordClass",
            "entry_sense", "word_id" };

    static final String[] columnListInExamples = { "example_text",
            "entry_id" };

    static final String[] tableListApp = {"dicts", "dict_types"};

    // static final String[] columnListInDicts = { "dict_name", "dict_shortName",
    //         "dict_type_id", "dict_size",  "dict_mtime", "dict_atime" };
    static final String[] columnListInDicts = { "dict_name", "dict_shortName",
            "dict_type_id", "dict_size"};

    // }}} static fields //

    public static String commaJoin(String... columns) {
        return String.join(", ", columns);
    }

    public static String getPlaceholder(String[] columnList) {
        String[] temp = columnList.clone();
        for (int i = 0; i < temp.length; i++) {
            temp[i] = "?";
        }
        return String.join(",", temp);
    }

    public static String hasTables(String keyword) {
        return "SHOW TABLES LIKE \'%" + keyword + "%\';";
    }

    // operate word in a dictionary {{{ //
    // query word {{{ //
    public static String queryWord(String shortName, String wordSpell) {
        String columns = commaJoin(columnListInQueryWord);

        String tables = tableStrDictQueryWord(shortName, tableListInDict);
        String wheres = whereStrDictQueryWord(shortName, wordSpell);
        String query = "SELECT " + columns + " FROM " + tables
            + " WHERE (" + wheres + ")";

        Utils.debug(query);
        return query;
    }

    public static String tableStrDictQueryWord(String shortName,
            String... tables) {
        String[] temp = tables.clone();
        for (int i = 0; i < temp.length; i++) {
            temp[i] = shortName + "_" + temp[i];
        }
        return String.join(", ", temp);
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

        String matchWordSpell = String.format("%s_words.word_spell = \'%s\'",
                shortName, wordSpell);

        String matchWordForms = String.format("%s_words.word_forms LIKE \'%s%%\'",
                shortName, wordSpell);
        conditions[3] = String.format("(%s OR %s)", matchWordSpell, matchWordForms);

        return String.join(" AND ", conditions);
    }
    // }}} query word //

    // update word {{{ //
    public static String selectWordCondition(String wordSpell) {
        return String.format(" WHERE word_spell = \'%s\'", wordSpell);
    }
    public static String updateWordAccess(String shortName,
            String wordSpell, int acounter) {
        String result = null;
        result = String.format("UPDATE %s_words "
            + "SET word_atime = CURRENT_TIMESTAMP(), "
            + "word_acounter = %d ", shortName, acounter)
            + selectWordCondition(wordSpell);

        Utils.debug(result);
        return result;
    }

    public static String updateWordModify(String shortName,
            String wordSpell) {
        String result = null;
        result = String.format("UPDATE %s_words "
            + "SET word_mtime = CURRENT_TIMESTAMP() ", shortName)
            + selectWordCondition(wordSpell);

        Utils.debug(result);
        return result;
    }

    public static String updateWordForms(String shortName,
            String wordSpell, ArrayList<String> wordForms) {
        String result = null;
        result = String.format("UPDATE %s_words "
            + "SET word_mtime = CURRENT_TIMESTAMP(), "
            + "word_forms = \'%s\' ", shortName, wordForms.toString())
            + selectWordCondition(wordSpell);

        Utils.debug(result);
        return result;
    }

    public static String updateWordPronounce(String shortName,
            String wordSpell, Pronounce pronounce) {
        String result = null;
        result = String.format("UPDATE %s_words "
            + "SET word_mtime = CURRENT_TIMESTAMP(), "
            + "word_pron_soundmark = \'%s\', "
            + "word_pron_sound = \'%s\' ",
            shortName,
            pronounce.getSoundmark(),
            pronounce.getSound())
            + selectWordCondition(wordSpell);

        Utils.debug(result);
        return result;
    }

    public static String updateWordSource(String shortName,
                String wordSpell, String source) {
            String result = null;
            result = String.format("UPDATE %s_words "
                + "SET word_mtime = CURRENT_TIMESTAMP(), "
                + "word_source = \'%s\' ",
                shortName,
                source)
                + selectWordCondition(wordSpell);

            Utils.debug(result);
            return result;
        }
    // }}} update word //

    // delete {{{ //
    public static String deleteWord(String shortName, String wordSpell) {
        String result = String.format(
                "DELETE FROM %s_words WHERE word_spell = \'%s\'",
                shortName, wordSpell);

        Utils.debug(result);
        return result;
    }
    // }}} delete //

    // Add {{{ //

    public static String insertValueIntoFrequenies(String shortName) {
        String columns = commaJoin(columnListInFrequencies);
        String placeholder = getPlaceholder(columnListInFrequencies);
        String result = String.format("INSERT INTO %s_frequencies(%s) VALUES(%s)", shortName, columns, placeholder);
        Utils.debug(result);
        return result;
    }

    public static String insertValueIntoWords(String shortName) {
        String columns = commaJoin(columnListInWords);
        String placeholder = getPlaceholder(columnListInWords);
        String result = String.format("INSERT INTO %s_words(%s) VALUES(%s)",
                shortName, columns, placeholder);
        Utils.debug(result);
        return result;

    }

    public static String insertValueIntoEntries(String shortName) {
        String columns = commaJoin(columnListInEntries);
        String placeholder = getPlaceholder(columnListInEntries);
        String result =  String.format("INSERT INTO %s_entries(%s) VALUES(%s)", shortName, columns, placeholder);
        Utils.debug(result);
        return result;
    }

    public static String insertValueIntoExamples(String shortName) {
        String columns = commaJoin(columnListInExamples);
        String placeholder = getPlaceholder(columnListInExamples);
        String result =  String.format("INSERT INTO %s_examples(%s) VALUES(%s)", shortName, columns, placeholder);
        Utils.debug(result);
        return result;
    }

    public static String queryFreId(String shortName, String fre_band){
        String result = String.format(
                "SELECT fre_id FROM %s_frequencies WHERE fre_band = \'%s\';",
                shortName, fre_band);
        Utils.debug(result);
        return result;
    }

    // }}} Add //

    // query size of a local dictionary database {{{ //
    public static String querySize(String shortName) {
        String result = String.format("SELECT COUNT(*) FROM %s_words",
                shortName);

        Utils.debug(result);
        return result;
    }

    // }}} query size of a local dictionary database //

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
              + "word_mtime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + "word_atime             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + "word_acounter          int       DEFAULT 0,"
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
        String result = addForeignKey(shortName, "words", "frequencies", "fre_id");

        Utils.debug(result);
        return result;
    }

    public static String addForeignKeyWordId(String shortName) {
        String onDeleteCascade = " ON DELETE CASCADE";
        String result = addForeignKey(shortName, "entries", "words", "word_id")
            + onDeleteCascade;

        Utils.debug(result);
        return result;
    }

    public static String addForeignKeyEntryId(String shortName) {
        String onDeleteCascade = " ON DELETE CASCADE";
        String result = addForeignKey(shortName, "examples", "entries", "entry_id")
            + onDeleteCascade;

        Utils.debug(result);
        return result;
    }

    public static String addForeignKey(String shortName,
            String tableA, String tableB, String foreignKey) {
        String constraintName = shortName + "_fk_" + tableA + "_" + tableB;
        tableA = shortName + "_" + tableA;
        tableB = shortName + "_" + tableB;

        return "ALTER TABLE " + tableA
            + " ADD CONSTRAINT " + constraintName
            + " FOREIGN KEY (" + foreignKey + ")"
            + " REFERENCES " + tableB + "(" + foreignKey + ")";
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

    // Create database for App {{{ //
    public static String createDb(String dbName) {
        return "CREATE DATABASE IF NOT EXISTS " + dbName;
    }
    // }}} Create database for App //

    // Create tables for App {{{ //
    public static String createTableDicts() {
        return "CREATE TABLE dicts"
            + "("
            + "  dict_id                int       NOT NULL AUTO_INCREMENT,"
            + "  dict_name              char(50)  NOT NULL UNIQUE,"
            + "  dict_shortName         char(50)  NULL UNIQUE,"
            + "  dict_type_id           int       NULL ,"
            + "  dict_size              int       DEFAULT 0,"
            + "  dict_mtime             TIMESTAMP NULL,"
            + "  dict_atime             TIMESTAMP NULL,"
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

    public static String insertValueIntoDictTypes(String dictType) {
        return "INSERT INTO dict_types(dict_type)"
                + "VALUES(\'" + dictType + "\');";
    }
    // }}} Create tables for App //

// register dicts table for App {{{ //
    public static String insertValueIntoDicts() {
        String columns = commaJoin(columnListInDicts);
        String placeholder = getPlaceholder(columnListInDicts);
        String result = String.format("INSERT INTO dicts(%s) VALUES(%s)",
                 columns, placeholder);
        Utils.debug(result);
        return result;
    }

    public static String queryDictTypeId(String dictType) {
        String result = String.format("SELECT dict_type_id FROM dict_types "
                        + "WHERE dict_type = \'%s\'", dictType);

        Utils.debug(result);
        return result;
    }

    public static String clearDicts() {
        String result = "DELETE FROM dicts";

        Utils.debug(result);
        return result;
    }

// }}} register dicts table for App //
}
