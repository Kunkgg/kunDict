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
    private Instant lastModify;

    public Word(String spell,
                Pronounce pronounce,
                Frequency frequency,
                ArrayList<String> forms) {

        this.spell = spell;
        this.pronounce = pronounce;
        this.frequency = frequency;
        this.forms = forms;
    }

    public String toString() {
        return String.format("[%s, %s, %s, %s, length of examples: %d]",
                this.spell,
                this.pronounce.toString(),
                this.frequency.toString(),
                this.forms.toString(),
                this.senseEntryList.size()
                );
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
    // }}} setter //
}

/**
 * Pronounce
 */
class Pronounce {
    public String soundmark;
    public String sound;

    public String toString() {
        return this.soundmark;
    }

}

/**
 * SenseEntry
 */
class SenseEntry {
    public String wordClass;
    public String sense;
    public ArrayList<String> examples;
}

/**
 * Frequency
 */
class Frequency {
    public String band;
    public String description;

    public String toString() {
        return String.format("frequency band: %s", this.band);
    }
}
