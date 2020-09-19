package kunDict;

public class SQLStr {
    public static String queryWord(String dbName, String wordSpell) {
        String[] columnList = {"word_spell",
                            "word_source",
                            "word_forms",
                            "word_pron_soundmark",
                            "word_pron_sound",
                            "fre_band",
                            "fre_description",
                            "entry_wordClass",
                            "entry_sense",
                            "example_text"};
        String columns = commaJoin(columnList);

        String[] tableList = {"words", "frequencies", "entries", "examples" };
        String tables = tableStrDictQueryWord(dbName, tableList);
        String wheres = whereStrDictQueryWord(dbName, wordSpell);
        String query = "SELECT " + columns + " FROM " + tables
            + " WHERE (" + wheres + ")";

        return query;
    }

    public static String commaJoin(String... columns) {
        return String.join(", ", columns);
    }

    public static String tableStrDictQueryWord(String dbName,
            String... tables) {

        for (int i = 0; i < tables.length; i++) {
            tables[i] = dbName + "_" + tables[i];
        }
        return String.join(", ", tables);
    }

    public static String joinConditionStrDict(String dbName, String table1, String table2, String foreignKey)
    {
        return dbName + "_" + table1 + "." + foreignKey
            + " = " + dbName + "_" + table2 + "." + foreignKey;
    }

    public static String whereStrDictQueryWord(String dbName,
            String wordSpell) {
        String[] conditions = new String[4];
        conditions[0] = joinConditionStrDict(dbName,
                            "words", "frequencies", "fre_id");
        conditions[1] = joinConditionStrDict(dbName,
                            "words", "entries", "word_id");
        conditions[2] = joinConditionStrDict(dbName,
                            "entries", "examples", "entry_id");
        conditions[3] = "words.word_spell = " + "\'" + wordSpell + "\'";

        return String.join(" AND ", conditions);
    }
}
