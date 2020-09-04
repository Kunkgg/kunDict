package kunDict;

public class Extractor {

    private String input;
    private String[] ability;

    public Extractor(String input, String ability) {
        this.input = input;
    }

    public static Word collins() {
       return new Word() ;
    }

    public static Word getWord(String text) {};

    public static String[] getAbility() {};

    public static Boolean ifCan(String ability) { return true; };

    // private String spell;
    // private Pronounce pronounce;
    // private String frequency;
    // private String[] forms;
    // private SenseEntry[] senseEntrylist;

    // public Word(String spell,
    //             Pronounce pronounce,
    //             String frequency,
    //             String[] forms,
    //             SenseEntry[] senseEntrylist) {

    //     this.spell = spell;
    //     this.pronounce = pronounce;
    //     this.frequency = frequency;
    //     this.forms = forms;
    //     this.senseEntrylist = senseEntrylist;
    // }
}
