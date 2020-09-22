package kunDict;

public class Formatter {
    private Word word;

    public Formatter(Word word) {
        this.word = word;
    }

    public Word getWord() {
        return this.word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void printText() {
        System.out.println("############################################");
        if (this.word.isEmypty()) {
            Utils.warning(word.getSpell() + " is not found.");
        } else {
            System.out.println(this.word.getSpell());
            System.out.println("Forms: " + this.word.getForms());
            System.out.println("Source: " + this.word.getSource());
            System.out.println("Soundmark: "
                    + this.word.getPronounce().getSoundmark());
            System.out.println("Sound: "
                    + this.word.getPronounce().getSound());
            System.out.println("FreBand: "
                    + this.word.getFrequency().getBand());
            System.out.println(
                    "FreDesc: " + this.word.getFrequency().getDescription());
            System.out.println("Senses List:");
            for (SenseEntry entry : this.word.getSenesEntries()) {
                System.out.println(entry);
            }
        }
        System.out.println("############################################");
    }

    public static String toAnki(Word word) {
        return "toAnki";
    }
}
