package kunDict;

public class SQLStr {
    public static String queryWord(String shortName, String wordSpell) {
        String[] columnList = {"word_spell",
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
        String columns = commaJoin(columnList);

        String[] tableList = {"words", "frequencies", "entries", "examples" };
        String tables = tableStrDictQueryWord(shortName, tableList);
        String wheres = whereStrDictQueryWord(shortName, wordSpell);
        String query = "SELECT " + columns + " FROM " + tables
            + " WHERE (" + wheres + ")";

        return query;
    }

    public static String commaJoin(String... columns) {
        return String.join(", ", columns);
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
}
