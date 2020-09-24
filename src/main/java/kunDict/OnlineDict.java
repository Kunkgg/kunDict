package kunDict;

abstract class OnlineDict extends Dict {

    public OnlineDict(String name, String shortName, String description) {
        super(name, shortName, description, DictType.Online);
    }

    public OnlineDict(){
    }

    // abstract Word queryWord(String wordSpell);
}
