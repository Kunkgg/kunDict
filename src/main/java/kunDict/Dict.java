package kunDict;

import java.io.IOException;
import java.sql.SQLException;

abstract class Dict {
    private String name;
    private String description;
    private DictType type;

    public Dict(String name, String description, DictType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public Dict(){
    }

    // getter and setter {{{ //
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(DictType type) {
        this.type = type;
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

    abstract Word query(String wordSpell) throws IOException, SQLException;
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
