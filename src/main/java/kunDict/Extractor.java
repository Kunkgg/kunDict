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
        // extract a word from collins website through Jsoup {{{ //
        Word word = null;
        Document doc = Jsoup.parse(this.input);

        // Elements dict = doc.select("div.dictionary.Cob_adv_US.dictentry");
        Elements dicts = doc.select("div.dictentry");
        Element dict = dicts.first();
        Utils.debug("dicts size: " + dicts.size());
        if (dicts.size() > 0) {

        String source = "Collins Online English Dictionary";
        String spell = dict.select("h2.h2_entry span.orth").text();
        Pronounce pronounce = new Pronounce();
        pronounce.setSoundmark(dict.select("span.pron").text());
        pronounce.setSound(
                dict.select("a.hwd_sound.audio_play_button")
                        .attr("data-src-mp3"));
        Frequency fre = new Frequency();
        fre.setBand(dict.select("span.word-frequency-img").attr("data-band"));
        fre.setDescription(
                dict.select("span.word-frequency-img").attr("title"));
        Elements formsEle = dict.select("span.form span.orth");
        ArrayList<String> forms = new ArrayList<>();
        for(Element formEle : formsEle) {
            String form = formEle.text();
            if (!forms.contains(form)) forms.add(form);
        }

        Elements entrys = dict.select("div.hom");
        ArrayList<SenseEntry> senseEntryList = new ArrayList<>();

        for (Element hom : entrys) {
            String wordClass = hom.select("span.pos").text();
            if (! wordClass.equals("")) {

                for (Element sense : hom.select("div.sense")){
                    SenseEntry senseEntry = new SenseEntry();
                    senseEntry.setWordClass(wordClass);
                    senseEntry.setSense(sense.select("div.def").text());
                    for (Element example : sense.select("span.quote")) {
                        senseEntry.addExample(example.text());
                    }
                    senseEntryList.add(senseEntry);
                }
            }
        }
        // }}} extract a word from collins website through Jsoup //

        word =  new Word(spell, pronounce, fre, forms, senseEntryList, source);
        }
        return word;
    }

    public Word longmanOnline() {
        // extract a word from longman website through Jsoup {{{ //
        Word word = null;
        Document doc = Jsoup.parse(this.input);

        Element wordFamily = doc.select("div.wordfams").first();
        Elements dicts = doc.select("span.dictentry");
        Element dict = dicts.first();
        Utils.debug("dicts size: " + dicts.size());
        if (dicts.size() > 0) {

        String source = "Longman Online English Dictionary";
        String spell = doc.select("h1.pagetitle").text();
        Pronounce pronounce = new Pronounce();
        pronounce.setSoundmark(dict.select("span.PRON").text());
        pronounce.setSound(
                dict.select("span.speaker.brefile")
                        .attr("data-src-mp3"));
        Frequency fre = new Frequency();
        fre.setBand(dict.select("span.FREQ").first().text());
        fre.setDescription(dict.select("span.FREQ").first().attr("title"));

        ArrayList<String> forms = new ArrayList<>();
        if(wordFamily != null) {
            Elements formsEleW = wordFamily.select(".w");
            for(Element formEle : formsEleW) {
                String form = formEle.text();
                if (!forms.contains(form)) forms.add(form);
            }
        }

        Elements entrys = dict.select("span.Sense");
        ArrayList<SenseEntry> senseEntryList = new ArrayList<>();

        for (Element entry : entrys) {
            String wordClass = entry.select("span.SIGNPOST").text();
            if (! wordClass.equals("")) {

                    SenseEntry senseEntry = new SenseEntry();
                    senseEntry.setWordClass(wordClass);
                    senseEntry.setSense(entry.select("span.DEF").text());
                    for (Element example : entry.select("span.EXAMPLE")) {
                        senseEntry.addExample(example.text());
                    }
                    senseEntryList.add(senseEntry);
            }
        }
        // }}} extract a word from longman website through Jsoup //

        word =  new Word(spell, pronounce, fre, forms, senseEntryList, source);
        }
        return word;
    }

}
