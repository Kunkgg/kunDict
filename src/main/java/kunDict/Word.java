package kunDict;

import java.time.Instant;
import java.util.ArrayList;
// import java.io.Serializable;

/**
 * Word
 */
// public class Word implements Serializable{
public class Word implements Cloneable {
    private String spell;
    private ArrayList<String> forms;
    private ArrayList<Frequency> frequencyList;
    private Pronounce pronounce;
    private ArrayList<SenseEntry> senseEntryList;
    private String source = "";
    // timestamp for last modify
    private Instant mtime;
    // timestamp for last access
    private Instant atime;
    // counter query
    private int acounter = 0;

    // constructors {{{ //
    public Word(String spell, Pronounce pronounce, ArrayList<Frequency> frequencyList,
            ArrayList<String> forms, ArrayList<SenseEntry> senseEntryList,
            String source) {

        this.spell = spell;
        this.pronounce = pronounce;
        this.frequencyList = frequencyList;
        this.forms = forms;
        this.senseEntryList = senseEntryList;
        this.source = source;
    }

    public Word(String spell, Pronounce pronounce, ArrayList<Frequency> frequencyList,
            ArrayList<String> forms, ArrayList<SenseEntry> senseEntryList,
            String source, int acounter, Instant mtime, Instant atime) {

        this.spell = spell;
        this.pronounce = pronounce;
        this.frequencyList = frequencyList;
        this.forms = forms;
        this.senseEntryList = senseEntryList;
        this.source = source;
        this.acounter = acounter;
        this.mtime = mtime;
        this.atime = atime;
    }
    // }}} constructors //

    private ArrayList<Frequency> cloneArrayListFrequencies(ArrayList<Frequency> arrayListFrequencies)
            throws CloneNotSupportedException {
        ArrayList<Frequency> container = new ArrayList<>();

        Object clonedList = arrayListFrequencies.clone();
        if (clonedList instanceof ArrayList<?>) {
            ArrayList<?> clonedObjList = (ArrayList<?>) clonedList;
            for(Object obj : clonedObjList) {
                if (obj instanceof Frequency) {
                    Frequency fre = (Frequency) obj;
                    container.add(fre.clone());
                }
            }
        }

        return container;
    }

    private ArrayList<SenseEntry> cloneArrayListSenseEntries(ArrayList<SenseEntry> arrayListSenseEntries)
            throws CloneNotSupportedException {
        ArrayList<SenseEntry> container = new ArrayList<>();

        Object clonedList = arrayListSenseEntries.clone();
        if (clonedList instanceof ArrayList<?>) {
            ArrayList<?> clonedObjList = (ArrayList<?>) clonedList;
            for(Object entry : clonedObjList) {
                if(entry instanceof SenseEntry) {
                    SenseEntry clonedEntry = (SenseEntry) entry;
                    container.add(clonedEntry.clone());
                }
            }
        }

        return container;
    }

    @Override
    public Word clone() throws CloneNotSupportedException {
        Word cloned = (Word) super.clone();

        ArrayList<String> clonedForms = Utils.cloneArrayListString(
                cloned.getForms());

        ArrayList<Frequency> clonedFrequencyList =
            cloneArrayListFrequencies(cloned.getFrequencies());
        ArrayList<SenseEntry> clonedSenseEntryList =
            cloneArrayListSenseEntries(cloned.getSenseEntries());
        Pronounce clonedPronounce = cloned.getPronounce().clone();

        cloned.setForms(clonedForms);
        cloned.setFrequencies(clonedFrequencyList);
        cloned.setPronounce(clonedPronounce);
        cloned.setSenseEntries(clonedSenseEntryList);

        return cloned;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s]%n%s, %s, %s, %s, length of examples: %d%nFirst entry:%n%s",
                this.source, this.spell, this.pronounce.toString(),
                this.frequencyList.toString(), this.forms.toString(),
                this.senseEntryList.size(),
                this.senseEntryList.get(0).toString());
    }

    @Override
    public boolean equals(Object otherObj) {
        if (this == otherObj) return true;
        if (otherObj == null) return false;
        if (this.getClass() != otherObj.getClass()) return false;
        Word other = (Word) otherObj;

        return (this.spell.equals(other.getSpell()) &&
                this.forms.equals(other.getForms()) &&
                this.pronounce.equals(other.getPronounce()) &&
                this.frequencyList.equals(other.getFrequencies()) &&
                this.senseEntryList.equals(other.getSenseEntries()) &&
                this.source.equals(other.getSource()));
    }

    public boolean isEmypty() {
        if (this.senseEntryList == null || this.senseEntryList.size() == 0) {
            return true;
        }
        return false;
    }

    // getter {{{ //
    public String getSpell() {
        return this.spell;
    }

    public Pronounce getPronounce() {
        return this.pronounce;
    }

    public ArrayList<Frequency> getFrequencies() {
        return this.frequencyList;
    }

    public ArrayList<String> getForms() {
        return this.forms;
    }

    public ArrayList<SenseEntry> getSenseEntries() {
        return this.senseEntryList;
    }

    public Instant getMtime() {
        return this.mtime;
    }

    public Instant getAtime() {
        return this.atime;
    }

    public String getSource() {
        return this.source;
    }

    public int getAcounter() {
        return this.acounter;
    }
    // }}} getter //

    // setter {{{ //
    private void updateMtime() {
        this.mtime = Instant.now();
    }

    private void updateAtime() {
        this.atime = Instant.now();
    }

    public void setAtime(Instant atime) {
        this.atime = atime;
    }

    public void setAcounter(int acounter) {
        this.acounter = acounter;
    }

    public void setSpell(String spell) {
        this.spell = spell;
        updateMtime();
    }

    public void setPronounce(Pronounce pronounce) {
        this.pronounce = pronounce;
        updateMtime();
    }

    public void setFrequencies(ArrayList<Frequency> frequencyList) {
        this.frequencyList = frequencyList;
        updateMtime();
    }

    public void setForms(ArrayList<String> forms) {
        this.forms = forms;
        updateMtime();
    }

    public void setSenseEntries(ArrayList<SenseEntry> senseEntryList) {
        this.senseEntryList = senseEntryList;
        updateMtime();
    }

    public void setSource(String source) {
        this.source = source;
        updateMtime();
    }
    // }}} setter //
}

/**
 * Pronounce
 */
class Pronounce implements Cloneable{
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

    public boolean equals(Object otherObj) {
        if (this == otherObj) return true;
        if (otherObj == null) return false;
        if (this.getClass() != otherObj.getClass()) return false;
        Pronounce other = (Pronounce) otherObj;

        return (this.soundmark.equals(other.getSoundmark()) &&
                this.sound.equals(other.getSound()));
    }

    public Pronounce clone() throws CloneNotSupportedException {
        return (Pronounce) super.clone();
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
class SenseEntry implements Cloneable {
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

    public boolean equals(Object otherObj) {
        if (this == otherObj) return true;
        if (otherObj == null) return false;
        if (this.getClass() != otherObj.getClass()) return false;
        SenseEntry other = (SenseEntry) otherObj;

        return (this.wordClass.equals(other.getWordClass()) &&
                this.sense.equals(other.getSense()) &&
                this.examples.equals(other.getExamples()));
    }

    public SenseEntry clone() throws CloneNotSupportedException {
        SenseEntry cloned = (SenseEntry) super.clone();
        ArrayList<String> clonedExamples = Utils.cloneArrayListString(cloned.getExamples());
        cloned.setExamples(clonedExamples);

        return cloned;
    }

    /**
     * combine
     * combine with other duplicated {@link #SenseEntry} instance
     * Duplicated senseEntries have same wordClass and sense,
     * regardless whether they have same examples.
     */
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

    /**
     * noDuplicatedSense
     * Remove all duplicated senseEntry elements in specific senseEntryList
     */
    public static ArrayList<SenseEntry> noDuplicatedSense(
            ArrayList<SenseEntry> senseEntryList) {
        for (int i = 0; i < senseEntryList.size() - 1; i++) {
            for (int j = i + 1; j < senseEntryList.size(); j++) {
                SenseEntry entryI = senseEntryList.get(i);
                SenseEntry entryJ = senseEntryList.get(j);

                if (entryI != null && entryJ != null
                        && entryI.getWordClass().equals(entryJ.getWordClass())
                        && entryI.getSense().equals(entryJ.getSense())) {
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
class Frequency implements Cloneable {
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

    public boolean equals(Object otherObj) {
        if (this == otherObj) return true;
        if (otherObj == null) return false;
        if (this.getClass() != otherObj.getClass()) return false;
        Frequency other = (Frequency) otherObj;

        return (this.band.equals(other.getBand()) &&
                this.description.equals(other.getDescription()));
    }

    public Frequency clone() throws CloneNotSupportedException {
        return (Frequency) super.clone();
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
