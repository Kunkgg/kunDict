package kunDict;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Word
 */
public class Word {
    private String spell;
    private ArrayList<String> forms;
    private Frequency frequency;
    private Pronounce pronounce;
    private ArrayList<SenseEntry> senseEntryList;
    private String source = "";
    private Instant lastModify;

    public Word(String spell, Pronounce pronounce, Frequency frequency,
            ArrayList<String> forms, ArrayList<SenseEntry> senseEntryList,
            String source) {

        this.spell = spell;
        this.pronounce = pronounce;
        this.frequency = frequency;
        this.forms = forms;
        this.senseEntryList = senseEntryList;
        this.source = source;
    }

    public String toString() {
        return String.format(
                "[%s]%n[%s, %s, %s, %s, length of examples: %d]%nFirst entry:%n%s",
                this.source, this.spell, this.pronounce.toString(),
                this.frequency.toString(), this.forms.toString(),
                this.senseEntryList.size(),
                this.senseEntryList.get(0).toString());
    }

    // getter {{{ //
    public String getSpell() {
        return this.spell;
    }

    public Pronounce getPronounce() {
        return this.pronounce;
    }

    public Frequency getFrequency() {
        return this.frequency;
    }

    public ArrayList<String> getforms() {
        return this.forms;
    }

    public ArrayList<SenseEntry> getSenesEntry() {
        return this.senseEntryList;
    }

    public Instant getLastModify() {
        return this.lastModify;
    }

    public String getSource() {
        return this.source;
    }

    // }}} getter //

    // setter {{{ //
    private void setLastModify() {
        this.lastModify = Instant.now();
    }

    public void setSpell(String spell) {
        this.spell = spell;
        setLastModify();
    }

    public void setPronounce(Pronounce pronounce) {
        this.pronounce = pronounce;
        setLastModify();
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        setLastModify();
    }

    public void setforms(ArrayList<String> forms) {
        this.forms = forms;
        setLastModify();
    }

    public void setSenesEntry(ArrayList<SenseEntry> senseEntryList) {
        this.senseEntryList = senseEntryList;
        setLastModify();
    }

    public void setSource(String source) {
        this.source = source;
        setLastModify();
    }
    // }}} setter //
}

/**
 * Pronounce
 */
class Pronounce {
    private String soundmark;
    private String sound;

    public Pronounce() {
    }

    public Pronounce(String soundmark, String sound) {
        this.soundmark = soundmark;
        this.sound = sound;
    }

    public String toString() {
        return this.soundmark;
    }

    // getter and setter {{{ //
    public String getSoundmark() {
        return this.soundmark;
    }

    public String getSound() {
        return this.sound;
    }

    public void setSoundmark(String soundmark) {
        this.soundmark = soundmark;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
    // }}} getter and setter //
}

/**
 * SenseEntry
 */
class SenseEntry {
    private String wordClass;
    private String sense;
    private ArrayList<String> examples = new ArrayList<>();

    public String toString() {
        String entry = String.format("%s, %s%n", this.wordClass, this.sense);
        for (String example : examples) {
            entry = entry + example + "\n";
        }
        return entry;
    }

    public void combine(SenseEntry other) {
        if (this.getWordClass().equals(other.getWordClass())
                && this.getSense().equals(other.getSense())) {
            for (String example : other.getExamples()) {
                if (!this.getExamples().contains(example)) {
                    this.addExample(example);
                }
            }
        }
    }

    public static ArrayList<SenseEntry> noDeuplicateItem(ArrayList<SenseEntry> senseEntryList) {
        for (int i = 0; i < senseEntryList.size() - 1; i++) {
            for (int j = i + 1; j < senseEntryList.size(); j++) {
                SenseEntry entryI = senseEntryList.get(i);
                SenseEntry entryJ = senseEntryList.get(j);
                if(entryI.getWordClass().equals(entryJ.getWordClass()) &&
                    entryI.getSense().equals(entryJ.getSense())) {
                        entryI.combine(entryJ);
                        senseEntryList.set(i, entryI);
                        senseEntryList.set(j, null);
                    }
            }
        }
        senseEntryList.removeIf(n -> (n == null));
        return senseEntryList;
    }

    // getter and setter {{{ //
    public String getWordClass() {
        return this.wordClass;
    }

    public String getSense() {
        return this.sense;
    }

    public ArrayList<String> getExamples() {
        return this.examples;
    }

    public void setWordClass(String wordClass) {
        this.wordClass = wordClass;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public void setExamples(ArrayList<String> examples) {
        this.examples = examples;
    }

    public void addExample(String example) {
        this.examples.add(example);
    }
    // }}} getter and setter //
}

/**
 * Frequency
 */
class Frequency {
    private String band;
    private String description;

    public Frequency() {
    }

    public Frequency(String band, String description) {
        this.band = band;
        this.description = description;
    }

    public String toString() {
        return String.format("frequency band: %s", this.band);
    }

    // getter and setter {{{ //
    public String getBand() {
        return this.band;
    }

    public String getDescription() {
        return this.description;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    // }}} getter and setter //

}
