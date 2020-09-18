package kunDict;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Extractor {

    private String input;

    public Extractor(String input) {
        this.input = input;
    }
    // ability, not implemented {{{ //

    // public static ArrayList<String> ability = new ArrayList<>(
    // Arrays.asList("collionsOnline"));

    // public Extractor(String input, String ability) {
    // this.input = input;
    // }

    // public static boolean checkAbility(String ability) {
    // return ability.indexOf(ability) > -1;
    // }

    // public static ArrayList<String> getAbility() {
    // ArrayList<String> result = (ArrayList<String>) ability.clone();
    // return result;
    // }

    // public static Word getWord(String text) {};

    // public static String[] getAbility() {};

    // public static Boolean ifCan(String ability) { return true; };

    // }}} ability, not implemented //
    public Word collinsOnline() {
        Document doc = Jsoup.parse(this.input);
        Elements dict = doc.select("div.dictionary.Cob_adv_US.dictentry");

        String source = "Collins Online English Dictionary";
        String spell = dict.select("h2.h2_entry span.orth").text();
        Pronounce pronounce = new Pronounce();
        pronounce.setSoundmark(dict.select("div.mini_h2 span.pron").text());
        pronounce.setSound(
                dict.select("div.mini_h2 a.hwd_sound.audio_play_button")
                        .attr("data-src-mp3"));
        Frequency fre = new Frequency();
        fre.setBand(dict.select("span.word-frequency-img").attr("data-band"));
        fre.setDescription(
                dict.select("span.word-frequency-img").attr("title"));
        Elements formsEle = dict.select("span.form span.orth");
        ArrayList<String> forms = new ArrayList<>();
        for(Element formEle : formsEle) {
            forms.add(formEle.text());
        }

        Elements entrys = dict.select("div.hom");
        ArrayList<SenseEntry> senseEntryList = new ArrayList<>();

        for (Element entry : entrys) {
            String wordClass = entry.select("span.gramGrp").text();
            if (! wordClass.equals("")) {
                SenseEntry senseEntry = new SenseEntry();
                senseEntry.setWordClass(wordClass);
                senseEntry.setSense(entry.select("div.def").text());
                for (Element example : entry.select("div.type-example")) {
                    senseEntry.addExample(example.text());
                }

                senseEntryList.add(senseEntry);
            }
        }

        return new Word(spell, pronounce, fre, forms, senseEntryList, source);
}
}
