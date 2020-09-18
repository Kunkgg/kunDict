package kunDict;

import java.time.Instant;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

abstract class LocalDict extends Dict{
    private Instant lastModify;
    private String dbName;

    public LocalDict(String name, String description, DictType type) {
        super(name, description, type);
        this.setType(DictType.Local);
    }

    public LocalDict(){
    }

    public Instant getLastModify(){
        return this.lastModify;
    }

    private void updateLastModify(){
        this.lastModify = Instant.now();
    }

    public String getDbName() {
       return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }


    public Word query(String wordSpell) throws IOException, SQLException {
        Word word = null;
        Database db = new Database(this.dbName);
        Connection con = db.getConnection();

        // query from locale database
        try (Statement stmt = con.createStatement();) {
        // make query string for querying a word {{{ //
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
        // }}} make query string for querying a word //
            System.out.println(query);

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);

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
                count++;
            }

            senseEntryList = SenseEntry.noDuplicatedSense(senseEntryList);
            word = new Word(wordSpell, pron, fre, forms, senseEntryList,
                    source);
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

            // }}} process the ResultSet //
        return word;
    };

    // abstract Boolean add(Word word);
    // abstract Boolean delete(String wordSpell);
    // abstract Word random();
    // abstract Boolean update(Word word);
    // abstract int size();
    // abstract Boolean generate();

}
