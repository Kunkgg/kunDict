package kunDict;

import java.util.ArrayList;
import java.util.Arrays;

public class Extractor {

    private String input;
    public static ArrayList<String> ability = new ArrayList<>(
            Arrays.asList("collionsOnline"));

    public Extractor(String input, String ability) {
        this.input = input;
    }


    public static boolean checkAbility(String ability) {
        return ability.indexOf(ability) > -1;
    }

    public static ArrayList<String> getAbility() {
        ArrayList<String> result = (ArrayList<String>) ability.clone();
        return result;
    }

    public Word collinsOnline() {
       return new Word() ;
    }

    // public static Word getWord(String text) {};

    // public static String[] getAbility() {};

    // public static Boolean ifCan(String ability) { return true; };
}
