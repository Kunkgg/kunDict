package kunDict;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

abstract class LocalDict extends Dict{
    private Instant timestamp;
    private Database db;
    // shortName is the short name of Dict.name
    // It is used to be prefix of each tables of respective dictionary.
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

    public Database getDb() {
        return this.db;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setDb(Database db){
        this.db = db;
    }

    // }}} getter and setter //

    // manage each dict {{{ //
    public void initializeTables() throws IOException, SQLException {
        this.db.getConnectionUseDbName();

        if (!hasTables()) {
            db.createTable(SQLStr.createTableWords(this.shortName));
            db.createTable(SQLStr.createTableFrequencies(this.shortName));
            db.createTable(SQLStr.createTableEntries(this.shortName));
            db.createTable(SQLStr.createTableExamples(this.shortName));
            db.addForeignKey(SQLStr.addForeignKeyFreId(this.shortName));
            db.addForeignKey(SQLStr.addForeignKeyWordId(this.shortName));
            db.addForeignKey(SQLStr.addForeignKeyEntryId(this.shortName));
        }

        Utils.info(this.shortName + " dictionary INITED");
    }

    public void dropTables() throws IOException, SQLException {
        this.db.getConnectionUseDbName();
        db.dropTable(SQLStr.dropTableInDict(this.shortName));

        Utils.info(this.shortName + " dictionary tables DELETED");
    }

    public boolean hasTables() throws IOException, SQLException {
        Boolean result = false;
        Connection con = this.db.getCurrentConUseDb();

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.hasTables(this.shortName);

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<String> existedTables = new ArrayList<>();
            ArrayList<String> designedTables = new ArrayList<>(
                    Arrays.asList(SQLStr.tableListInDict));
            while (rs.next()) {
                existedTables.add(rs.getString(1));
            }
            for (int i = 0; i < designedTables.size(); i++) {
                designedTables.set(i,
                        this.shortName + "_" + designedTables.get(i));
            }
            result = existedTables.containsAll(designedTables);

        } catch (SQLException e) {
            Database.printSQLException(e);
        }
        if (result) {
            Utils.info(this.shortName + " dictionary tables existed");
        } else {
            Utils.info(this.shortName + " dictionary tables NOT existed");
        }

        return result;
        // }}} process the ResultSet //
    }

    // public boolean checkForeignKey(){}

    // }}} manage each dict //

    // operater in dictionary {{{ //
    public Word queryWord(String wordSpell) throws IOException, SQLException {
        Word word = null;
        Connection con = this.db.getCurrentConUseDb();

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
                if (count == 0) {
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
    // }}} operater in dictionary //
    private PreparedStatement setPstmtFrequencies(PreparedStatement pstmt,
            Word word) throws SQLException{
            pstmt.setInt(1, Integer.parseInt(word.getFrequency().getBand()));
            pstmt.setString(2, word.getFrequency().getDescription());

            return pstmt;
    }

    private PreparedStatement setPstmtWords(PreparedStatement pstmt,
            Word word) throws SQLException{
            pstmt.setString(1, word.getSpell());
            pstmt.setString(2, word.getSource());
            pstmt.setString(3, word.getForms().toString());
            pstmt.setString(4, word.getPronounce().getSoundmark());
            pstmt.setString(5, word.getPronounce().getSound());

            return pstmt;
    }

    private PreparedStatement setPstmtEntries(PreparedStatement pstmt,
            String entry_wordClass, String entry_sense) throws SQLException{
            pstmt.setString(1, entry_wordClass);
            pstmt.setString(2, entry_sense);

            return pstmt;
    };

    private PreparedStatement setPstmtExamples(PreparedStatement pstmt,
            String example_text) throws SQLException{
            pstmt.setString(1, example_text);

            return pstmt;
    };

    public void addWord(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Could't add a empty word to database.");
        } else {
            Connection con = this.db.getCurrentConUseDb();
            PreparedStatement pstmtFrequencies = null;
            PreparedStatement pstmtWords = null;
            PreparedStatement pstmtEntries = null;
            PreparedStatement pstmtExamples = null;
            ResultSet rs = null;
            int rowAffected = 0;
            int freId = 0;
            int wordId = 0;
            int entryId = 0;

            try {
                con.setAutoCommit(false);

                pstmtFrequencies = con.prepareStatement(
                        SQLStr.insertValueIntoFrequenies(this.shortName),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtWords = con.prepareStatement(
                        SQLStr.insertValueIntoWords(this.shortName),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtEntries = con.prepareStatement(
                        SQLStr.insertValueIntoEntries(this.shortName),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtExamples = con.prepareStatement(
                        SQLStr.insertValueIntoExamples(this.shortName),
                        Statement.RETURN_GENERATED_KEYS);

                pstmtFrequencies = setPstmtFrequencies(pstmtFrequencies, word);
                rowAffected = pstmtFrequencies.executeUpdate();
                rs = pstmtFrequencies.getGeneratedKeys();
                if (rs.next()) {
                    freId = rs.getInt(1);
                    rs.close();
                }
                if (freId > 0) {

                    pstmtWords = setPstmtWords(pstmtWords, word);
                    pstmtWords.setInt(6, freId);
                    rowAffected = pstmtWords.executeUpdate();
                    rs = pstmtWords.getGeneratedKeys();

                    if (rs.next()) {
                        wordId = rs.getInt(1);
                        rs.close();
                    }

                    if (wordId > 0 && rowAffected == 1) {
                        for(SenseEntry entry : word.getSenesEntries()) {
                            pstmtEntries = setPstmtEntries(pstmtEntries,
                                    entry.getWordClass(), entry.getSense());
                            pstmtEntries.setInt(3, wordId);
                            rowAffected = pstmtEntries.executeUpdate();
                            rs = pstmtEntries.getGeneratedKeys();
                            if (rs.next()) {
                                entryId = rs.getInt(1);
                                rs.close();
                            }
                            if (entryId > 0 && rowAffected == 1) {
                                for(String example : entry.getExamples()) {
                                    pstmtExamples = setPstmtExamples(pstmtExamples, example);
                                    pstmtExamples.setInt(2, entryId);
                                    pstmtExamples.executeUpdate();
                                }
                            }
                        }

                    }
                }

                con.commit();
                Utils.info("Added a word (" + word.getSpell() + ") to "
                        + this.getName() + "database");
            } catch (SQLException e) {
                try {
                    if (con != null)
                        con.rollback();
                } catch (SQLException ex) {
                    Database.printSQLException(ex);
                }
                Database.printSQLException(e);
            } finally {
                // finally {{{ //
                try {
                    if (rs != null)
                        rs.close();
                    if (pstmtFrequencies != null) {
                        pstmtFrequencies.close();
                    }
                    if (pstmtWords != null) {
                        pstmtWords.close();
                    }
                    if (pstmtEntries != null) {
                        pstmtEntries.close();
                    }
                    if (pstmtExamples != null) {
                        pstmtExamples.close();
                    }
                    if (con != null)
                        con.setAutoCommit(true);
                        con.close();
                // }}} finally //
                } catch (SQLException e) {
                    Database.printSQLException(e);
                }
            }
        }

    }

    // public void insertValuesIntoFrequenies(Word word) {

    // }



    // abstract Boolean add(Word word);
    // abstract Boolean delete(String wordSpell);
    // abstract Word random();
    // abstract Boolean update(Word word);
    // abstract int size();
    // abstract Boolean generate();

}
