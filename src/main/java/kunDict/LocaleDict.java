package kunDict;

import java.time.Instant;

abstract class LocaleDict extends Dict{
    private Instant lastModify;

    public LocaleDict(String name, String description, DictType type) {
        super(name, description, type);
    }

    public Instant getLastModify(){
        return this.lastModify;
    }

    private void updateLastModify(){
        this.lastModify = Instant.now();
    }

    abstract Boolean add(Word word);
    abstract Boolean delete(String wordSpell);
    abstract Word query(String wordSpell);
    abstract Word random();
    abstract Boolean update(Word word);
    abstract int size();
    abstract Boolean generate();

}
