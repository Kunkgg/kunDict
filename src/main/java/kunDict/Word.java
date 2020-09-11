package kunDict;

import java.time.Instant;

/**
 * Word
 */
public class Word {
    private String spell;
    private String[] forms;
    private String frequency;
    private Pronounce pronounce;
    private SenseEntry[] senseEntryList;
    private Instant lastModify;

    public Word(String spell,
                Pronounce pronounce,
                String frequency,
                String[] forms,
                SenseEntry[] senseEntrylist) {

        this.spell = spell;
        this.forms = forms;
        this.frequency = frequency;
        this.pronounce = pronounce;
        this.senseEntryList = senseEntryList;
    }

    public String toString() {
        return "word";
    }

    // getter {{{ //
    public String getSpell() {
        return this.spell;
    }

    public Pronounce getPronounce() {
        return this.pronounce;
    }

    public String getFrequency() {
        return this.frequency;
    }

    public String[] getforms() {
        return this.forms;
    }

    public SenseEntry[] getSenesEntry() {
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

    public void setFrequency(String frequency) {
        this.frequency = frequency;
        setLastModify();
    }

    public void setforms(String[] forms) {
        this.forms = forms;
        setLastModify();
    }

    public void setSenesEntry(SenseEntry[] senseEntryList) {
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
    public String[] sound;
}

/**
 * SenseEntry
 */
class SenseEntry {
    public String wordClass;
    public String sense;
    public String[] examples;
}
