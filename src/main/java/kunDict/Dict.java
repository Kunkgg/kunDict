package kunDict;

import java.time.LocalDateTime;

abstract class Dict {
    public String name;
    public String source;
    public String desc;
    private LocalDateTime lastModify;

    public Dict(String name, String source, String desc) {
        this.name = name;
        this.source = source;
        this.desc = desc;
    }

    abstract Boolean add(Word word);
    abstract Boolean delete(String wordSpell);
    abstract Word query(String wordSpell);
    abstract Word random();
    abstract Boolean update(Word word);
    abstract int size();
    abstract Boolean generate();
}
