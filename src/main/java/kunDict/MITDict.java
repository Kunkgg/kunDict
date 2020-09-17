package kunDict;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class MITDict extends Dict {

    final private String dbName = "mit_10k_dict";

    public MITDict() {
        this.setName("MIT 10K Englinsh Dictionary");
        this.setDescription(
                "Word list is from MIT. Word entries are from Collins online dictionary.");
        this.setType(DictType.Locale);
    }

    public Word query(String wordSpell) throws IOException, SQLException {
        Word word = null;
        Database db = new Database(this.dbName);
        Connection con = db.getConnection();
        // query from locale database
        try (Statement stmt = con.createStatement();) {
            String query = "SELECT word_spell, word_source, word_forms, "
                    + "word_pron_soundmark, word_pron_sound, fre_band, "
                    + "fre_description, entry_wordClass, "
                    + "entry_sense, example_text "
                    + "FROM words, frequencies, entries, examples "
                    + "WHERE ("
                    + "words.fre_id = frequencies.fre_id AND "
                    + "words.word_id = entries.word_id AND "
                    + "entries.entry_id = examples.entry_id AND "
                    + "words.word_spell = " + "\'" + wordSpell + "\')";

            System.out.println(query);

            ResultSet rs = stmt.executeQuery(query);

            // String source = rs.getString("word_source");
            // String soundmark = rs.getString("word_pron_soundmark");
            // String sound = rs.getString("word_pron_sound");
            // Pronounce pron = new Pronounce(soundmark, sound);
            // String freBand = String.valueOf(rs.getInt("fre_band"));
            // String freDescription = rs.getString("fre_description");
            // Frequency fre = new Frequency(freBand, freDescription);
            // ArrayList<String> forms = Utils.convertStringToArrayList(
            //         rs.getString("word_forms"));
            String source = null;
            Pronounce pron = null;
            Frequency fre = null;
            ArrayList<String> forms = null;

            ArrayList<SenseEntry> senseEntryList = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                if (count == 0){
                    source = rs.getString("word_source");
                    String soundmark = rs.getString("word_pron_soundmark");
                    String sound = rs.getString("word_pron_sound");
                    pron = new Pronounce(soundmark, sound);
                    String freBand = String.valueOf(rs.getInt("fre_band"));
                    String freDescription = rs.getString("fre_description");
                    fre = new Frequency(freBand, freDescription);
                    forms = Utils.convertStringToArrayList(
                            rs.getString("word_forms"));
                }

                SenseEntry senseEntry = new SenseEntry();
                senseEntry.setWordClass(rs.getString("entry_wordClass"));
                senseEntry.setSense(rs.getString("entry_sense"));
                senseEntry.addExample(rs.getString("example_text"));

                senseEntryList.add(senseEntry);
            }

            // TODO: fix SenseEntry.noDeuplicateItem method <17-09-20, gk07>
            // senseEntryList = SenseEntry.noDeuplicateItem(senseEntryList);
            word = new Word(wordSpell, pron, fre, forms, senseEntryList, source);
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
        return word;
    };

    // public Boolean add(Word word) {
    // };
    // public Boolean delete(String wordSpell);
    // public Word random();
    // public Boolean update(Word word);
    // public int size();
    // public Boolean generate();


}
