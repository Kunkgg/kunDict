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
            System.out.println();
            System.out.println(this.word.getSpell());
            System.out.println();
            System.out.println("Forms: " + this.word.getForms());
            System.out.println("Source: " + this.word.getSource());
            System.out.println("Soundmark: "
                    + this.word.getPronounce().getSoundmark());
            System.out.println("Sound: "
                    + this.word.getPronounce().getSound());
            for(Frequency fre : word.getFrequencies()) {
                System.out.println("FreBand: " + fre.getBand());
                System.out.println("FreDesc: " + fre.getDescription());
            }
            System.out.println();
            System.out.println("Senses List:");
            for (SenseEntry entry : this.word.getSenseEntries()) {
                System.out.println(entry);
            }
        }
        System.out.println("############################################");
    }

    public void printColorText() {
        System.out.println("############################################");
        if (this.word.isEmypty()) {
            Utils.warning(word.getSpell() + " is not found.");
        } else {
            System.out.println();
            ColorTerm.greenBoldPrintln(this.word.getSpell());
            System.out.println();
            System.out.println("Forms: " + this.word.getForms());
            System.out.println("Source: " + this.word.getSource());
            System.out.println("Soundmark: "
                    + this.word.getPronounce().getSoundmark());
            System.out.println("Sound: "
                    + this.word.getPronounce().getSound());
            for(Frequency fre : word.getFrequencies()) {
                System.out.println("FreBand: " + fre.getBand());
                System.out.println("FreDesc: " + fre.getDescription());
            }
            System.out.println();
            ColorTerm.magentaBoldPrintln("Senses List:");
            for (SenseEntry entry : this.word.getSenseEntries()) {
                ColorTerm.cyanPrint(entry.getWordClass());
                System.out.print(", ");
                System.out.print(entry.getSense());
                System.out.println();
                ColorTerm.blackPrintln("Examples:");
                for(String example : entry.getExamples()) {
                    System.out.println(example);
                }
                System.out.println();
            }
        }
        System.out.println("############################################");
    }
    public String toAnki() {
        return "toAnki";
    }
}
