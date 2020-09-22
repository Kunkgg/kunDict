package kunDict;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utils
 */
public class Utils {
    private static boolean info = true;
    private static boolean warning = true;
    private static boolean debug = true;

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
        if (info) {
        System.out.println("[INFO] " + String.join(", ", str));
        }
    }

    public static void warning(String... str) {
        if (warning) {
        System.out.println("[WARNING] " + String.join(", ", str));
        }
    }

    public static void debug(String... str) {
        if (debug) {
        System.out.println("[DEBUG] " + String.join(", ", str));
        }
    }

    public static void config(String... str) {
        System.out.println("[CONFIG] " + String.join(", ", str));
    }

    public static void err(String... str) {
        System.err.println("[ERROR] " + String.join(", ", str));
    }
}
