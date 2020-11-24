package kunDict;

import java.util.ArrayList;
import java.net.URISyntaxException;

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

    private static String getTextByCssSelector(Element parentEle,
            String cssSelector) {
        String text = "";

        Elements eles = parentEle.select(cssSelector);
        if (eles.size() > 0) {
            text = eles.first().text();
            Utils.debug(String.format(
                    "Extracted text \"%s\" by CSS selector \"%s\"", text,
                    cssSelector));
        } else {
            Utils.warning(String.format(
                    "CSS selector \"%s\" not matched anything."
                            + " This field was filled with empty string.",
                    cssSelector));
        }

        return text;
    }

    private static String getAttrByCssSelector(Element parentEle,
            String cssSelector, String attrName) {
        String attrText = "";

        Elements eles = parentEle.select(cssSelector);
        if (eles.size() > 0) {
            attrText = eles.first().attr(attrName);
            Utils.debug(String.format(
                    "Extracted attrText \"%s\" by CSS selector \"%s\"",
                    attrText, cssSelector));
        } else {
            Utils.warning(String.format(
                    "CSS selector \"%s\" not matched anything."
                            + " This field was filled with empty string.",
                    cssSelector));
        }

        return attrText;
    }

    private String makeCollinsSpell(Element dict) {
        String spell = getTextByCssSelector(dict, "h2.h2_entry span.orth");
        return spell;
    }

    private Pronounce makeCollionsPron(Element dict) {
        Pronounce pronounce = new Pronounce();
        String pronSoundmark = getTextByCssSelector(dict, "span.pron");
        String pronSound = getAttrByCssSelector(dict,
                "a.hwd_sound.audio_play_button", "data-src-mp3");
        pronounce.setSoundmark(pronSoundmark);
        pronounce.setSound(pronSound);
        return pronounce;
    }

    private ArrayList<Frequency> makeCollionsFreList(Element dict) {
        ArrayList<Frequency> freList = new ArrayList<>();
        Frequency fre = new Frequency();

        String freBand = getAttrByCssSelector(dict, "span.word-frequency-img",
                "data-band");
        String freDescription = getAttrByCssSelector(dict,
                "span.word-frequency-img", "title");
        fre.setBand(freBand);
        fre.setDescription(freDescription);
        freList.add(fre);
        return freList;
    }

    private ArrayList<String> makeCollionsForms(Element dict) {
        ArrayList<String> forms = new ArrayList<>();
        Elements formsEle = dict.select("span.form span.orth");
        for (Element formEle : formsEle) {
            String form = formEle.text();
            if (!forms.contains(form))
                forms.add(form);
        }

        return forms;
    }

    private ArrayList<SenseEntry> makeCollionsSenseEntryList(Element dict) {
        ArrayList<SenseEntry> senseEntryList = new ArrayList<>();

        Elements entrys = dict.select("div.hom");
        for (Element entry : entrys) {
            String wordClass = getTextByCssSelector(entry, "span.pos");

            if (!wordClass.equals("")) {
                for (Element sense : entry.select("div.sense")) {
                    SenseEntry senseEntry = new SenseEntry();
                    senseEntry.setWordClass(wordClass);
                    String def = getTextByCssSelector(sense, "div.def");
                    senseEntry.setSense(def);
                    for (Element example : sense.select("span.quote")) {
                        senseEntry.addExample(example.text());
                    }
                    senseEntryList.add(senseEntry);
                }
            }
        }

        return senseEntryList;
    }

    public Word collinsOnline() {
        // extract a word from collins website through Jsoup {{{ //
        Word word = null;
        Document doc = Jsoup.parse(this.input);

        Elements dicts = doc.select("div.dictentry");
        Element dict = dicts.first();
        Utils.debug("dicts size: " + dicts.size());
        if (dicts.size() > 0) {
            String source = CollinsOnlineDict.name;
            String spell = makeCollinsSpell(dict);
            Pronounce pronounce = makeCollionsPron(dict);
            ArrayList<Frequency> freList = makeCollionsFreList(dict);
            ArrayList<String> forms = makeCollionsForms(dict);
            ArrayList<SenseEntry> senseEntryList = makeCollionsSenseEntryList(
                    dict);
            // }}} extract a word from collins website through Jsoup //

            word = new Word(spell, pronounce, freList, forms, senseEntryList,
                    source);
        }
        return word;
    }

    private ArrayList<Frequency> makeLongmanFreList(Element dict) {
        ArrayList<Frequency> freList = new ArrayList<>();

        Elements toolTipLevelElements = dict.select("span.tooltip.LEVEL");
        for (Element ele : toolTipLevelElements) {
            Frequency fre = new Frequency();
            fre.setBand(ele.text());
            fre.setDescription(ele.attr("title"));
            freList.add(fre);
        }
        Elements freqElements = dict.select("span.FREQ");
        for (Element ele : freqElements) {
            Frequency fre = new Frequency();
            fre.setBand(ele.text());
            fre.setDescription(ele.attr("title"));
            freList.add(fre);
        }
        return freList;
    }

    private String makeLongmanSpell(Document doc) {
        String spell = getTextByCssSelector(doc, "h1.pagetitle");
        return spell;
    }

    private Pronounce makeLongmanPron(Element dict) {
        Pronounce pronounce = new Pronounce();

        String pronSoundmark = getTextByCssSelector(dict, "span.PRON");
        String pronSound = getAttrByCssSelector(dict, "span.speaker.brefile",
                "data-src-mp3");
        pronounce.setSoundmark(pronSoundmark);
        pronounce.setSound(pronSound);

        return pronounce;
    }

    private ArrayList<String> makeLongmanForms(Document doc) {
        ArrayList<String> forms = new ArrayList<>();

        Element wordFamily = doc.select("div.wordfams").first();
        if (wordFamily != null) {
            Elements formsEleW = wordFamily.select(".w");
            for (Element formEle : formsEleW) {
                String form = formEle.text();
                if (!forms.contains(form))
                    forms.add(form);
            }
        }

        return forms;
    }

    private ArrayList<SenseEntry> makeLongmanSenseEntryList(Element dict) {
        ArrayList<SenseEntry> senseEntryList = new ArrayList<>();

        Elements entrys = dict.select("span.Sense");
        for (Element entry : entrys) {
            String wordClass = getTextByCssSelector(dict,
                    ".dictentry .frequent.Head .POS");

            String def = getTextByCssSelector(entry, "span.DEF");
            if (!def.equals("") || !wordClass.equals("")) {
                SenseEntry senseEntry = new SenseEntry();
                senseEntry.setWordClass(wordClass);
                senseEntry.setSense(def);
                for (Element example : entry.select("span.EXAMPLE")) {
                    senseEntry.addExample(example.text());
                }
                senseEntryList.add(senseEntry);
            }
        }

        return senseEntryList;
    }

    private Document makeLongmanRedirect(Document doc) {
        Document newDoc = doc;

        // Justify if this page is a wordIndex page after redirection
        Elements searchTitle = doc.select(".search_title");
        if (searchTitle.size() > 0
                && searchTitle.first().text().equals("Did you mean:")) {
            String realTarget = getAttrByCssSelector(doc,
                    "ul.didyoumean li:first-child a", "href");
            if (!realTarget.equals("")) {
                try {
                    String url = LongmanOnlineDict.homePage + realTarget;
                    Request req = new Request(url);
                    String html = req.get().body();
                    newDoc = Jsoup.parse(html);
                } catch (URISyntaxException e) {
                    Utils.warning(
                            "Syntax error. Please check the spell of word.");
                }
            }
        }

        return newDoc;
    }

    public Word longmanOnline() {
        Word word = null;
        Document doc = Jsoup.parse(this.input);
        doc = makeLongmanRedirect(doc);

        Elements dicts = doc.select("span.dictentry");
        Element dict = dicts.first();
        Utils.debug("dicts size: " + dicts.size());
        if (dicts.size() > 0) {
            String source = LongmanOnlineDict.name;
            String spell = makeLongmanSpell(doc);
            Pronounce pronounce = makeLongmanPron(dict);
            ArrayList<String> forms = makeLongmanForms(doc);
            ArrayList<Frequency> freList = makeLongmanFreList(dict);
            ArrayList<SenseEntry> senseEntryList = makeLongmanSenseEntryList(
                    dict);

            word = new Word(spell, pronounce, freList, forms, senseEntryList,
                    source);
        }
        return word;
    }
}
