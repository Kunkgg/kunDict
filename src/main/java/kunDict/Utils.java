package kunDict;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Utils
 */
public class Utils {

    private static boolean configMsg = testString(App.configs.getProperty("configMsg"));
    private static boolean infoMsg = testString(App.configs.getProperty("infoMsg"));
    private static boolean warningMsg = testString(App.configs.getProperty("warningMsg"));
    private static boolean debugMsg = testString(App.configs.getProperty("debugMsg"));

    public static boolean testString(String str) {
        String[] trueValues = {"true", "True", "1"};
        List<String> trueList = Arrays.asList(trueValues);
        return trueList.contains(str);
    }

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
        if (infoMsg) {
        System.out.println("[INFO] " + String.join(", ", str));
        }
    }

    public static void warning(String... str) {
        if (warningMsg) {
        System.out.println("[WARNING] " + String.join(", ", str));
        }
    }

    public static void debug(String... str) {
        if (debugMsg) {
        System.out.println("[DEBUG] " + String.join(", ", str));
        }
    }

    public static void config(String... str) {
        if (configMsg) {
        System.out.println("[CONFIG] " + String.join(", ", str));
        }
    }

    public static void err(String... str) {
        System.err.println("[ERROR] " + String.join(", ", str));
    }

    public static void test(String... str) {
        System.err.println("[TEST] " + String.join(", ", str));
    }


    public static ArrayList<String> cloneArrayListString(ArrayList<String> arrayListString) {
        Object clonedFormsObj = arrayListString.clone();
        ArrayList<String> clonedList = new ArrayList<>();
        if (clonedFormsObj instanceof ArrayList<?>) {
            ArrayList<?> clonedFormsObjList = (ArrayList<?>) clonedFormsObj;
            for(Object clonedFormObj : clonedFormsObjList) {
                if (clonedFormObj instanceof String) {
                    String clonedForm = (String) clonedFormObj;
                    clonedList.add(clonedForm);
                }
            }
        }

        return clonedList;
    }
}
