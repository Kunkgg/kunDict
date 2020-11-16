package kunDict;

import java.util.ArrayList;

abstract class OnlineDict extends Dict {

    public OnlineDict(String name, String shortName, String description) {
        super(name, shortName, description, DictType.Online);
    }

    public OnlineDict(){
    }

    abstract ArrayList<Word> queryWordBySpell(String wordSpell);
}
