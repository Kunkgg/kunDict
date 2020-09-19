package kunDict;

import java.time.Instant;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

abstract class LocalDict extends Dict{
    private Instant timestamp;
    private final String dbName = "dict";
    private String shortName;

    public LocalDict(String name, String description, DictType type) {
        super(name, description, type);
        this.setType(DictType.Local);
    }

    public LocalDict(){
    }

    // getter and setter {{{ //
    public Instant getTimestamp(){
        return this.timestamp;
    }

    private void updateTimestamp(){
        this.timestamp = Instant.now();
    }

    public String getDbName() {
       return this.dbName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    // }}} getter and setter //

    public Word query(String wordSpell) throws IOException, SQLException {
        Word word = null;
        Database db = new Database(this.dbName);
        Connection con = db.getConnection();

        // query from locale database
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWord(this.shortName, wordSpell);
            System.out.println(query);

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);

            String source = null;
            Pronounce pron = null;
            Frequency fre = null;
            int counter = -1;
            Instant timestamp = null;
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
                    counter = rs.getInt("word_counter");
                    timestamp = rs.getTimestamp("word_timestamp").toInstant();
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
                    source, counter, timestamp);
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

            // }}} process the ResultSet //
        return word;
    };

    // public boolean add(Word word) {
    //     if (word.isEmypty()) {
    //         System.out.println(
    //                 "[Warning] Could't add a empty word to database.");
    //         return false;
    //     }

    // }


    // abstract Boolean add(Word word);
    // abstract Boolean delete(String wordSpell);
    // abstract Word random();
    // abstract Boolean update(Word word);
    // abstract int size();
    // abstract Boolean generate();

}
