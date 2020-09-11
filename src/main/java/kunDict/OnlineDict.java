package kunDict;

abstract class OnlineDict extends Dict {

    public OnlineDict(String name, String description, DictType type) {
        super(name, description, type);
    }

    abstract Word query(String wordSpell);
}
