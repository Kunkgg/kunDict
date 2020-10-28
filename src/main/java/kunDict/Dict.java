package kunDict;

import java.sql.SQLException;

abstract class Dict {
    private String name;
    // field shortName is the short name of Dict.name
    // It is used to be prefix of each tables of respective dictionary.
    private String shortName;
    private String description;
    private DictType type;

    public Dict(String name, String shortName,
            String description, DictType type) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.type = type;
    }

    public Dict(){
    }

    // getter and setter {{{ //
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(DictType type) {
        this.type = type;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public DictType getType() {
        return this.type;
    }
    // }}} getter and setter //

    public static String preProcessWordSpell(String wordSpell) {
        String[] filter = { "{", "}", "<", ">", "!", "@", "#", "$", "%", "^",
                "&", "*", "(", ")", "[", "]", "+", "=", "_", "|", "\\" };
        for (String c : filter) {
            wordSpell = wordSpell.replace(c, " ");
        }
        return wordSpell.strip();
    }

    abstract Word queryWord(String wordSpell) throws SQLException;
}

enum  DictType {
    Online {
        public String toString() {
            return "Online";
        }
    },
    Local {
        public String toString() {
            return "Local";
        }
    }
}
