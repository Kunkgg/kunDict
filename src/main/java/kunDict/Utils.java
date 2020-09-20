package kunDict;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utils
 */
public class Utils {

    /**
     * convertStringToArrayList
     * @param String str = "[waters, watering, watered]"
     * @return ArrayList<String> [waters, watering, watered]
     */
    public static ArrayList<String> convertStringToArrayList(String str) {

        String list[] = str.substring(1, str.length() - 1).split(", ");
        ArrayList<String> result = new ArrayList<>(Arrays.asList(list));

        return result;
    }

    public static void info(String... str) {
        System.out.println("[INFO] " + String.join(", ", str));
    }

    public static void warning(String... str) {
        System.out.println("[WARNING] " + String.join(", ", str));
    }

    public static void debug(String... str) {
        System.out.println("[DEBUG] " + String.join(", ", str));
    }

    public static void config(String... str) {
        System.out.println("[CONFIG] " + String.join(", ", str));
    }
}
