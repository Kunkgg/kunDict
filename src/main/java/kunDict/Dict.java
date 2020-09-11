package kunDict;

import java.time.LocalDateTime;

abstract class Dict {
    public String name;
    public String source;
    public String desc;
    private DictType type;
    private LocalDateTime lastModify;

    public Dict(String name, String source, String desc, DictType type) {
        this.name = name;
        this.source = source;
        this.desc = desc;
        this.type = type;
    }

    abstract Boolean add(Word word);
    abstract Boolean delete(String wordSpell);
    abstract Word query(String wordSpell);
    abstract Word random();
    abstract Boolean update(Word word);
    abstract int size();
    abstract Boolean generate();
}

enum  DictType {
    Online {
        public String toString() {
            return "Online Dict";
        }
    },
    Locale {
        public String toString() {
            return "Locale Dict";
        }
    },
}
