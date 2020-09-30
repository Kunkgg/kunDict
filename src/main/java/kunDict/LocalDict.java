package kunDict;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

abstract class LocalDict extends Dict {
    private Instant atime;
    private Instant mtime;
    private static Database db = App.db;
    // shortName is the short name of Dict.name
    // It is used to be prefix of each tables of respective dictionary.

    public LocalDict(String name, String shortName, String description) {
        super(name, shortName, description, DictType.Local);
    }

    public LocalDict() {
    }

    // getter and setter {{{ //
    public Instant getAtime() {
        return this.atime;
    }

    public Instant getMtime() {
        return this.mtime;
    }

    private void updateAtime() {
        this.atime = Instant.now();
    }

    private void updateMtime() {
        this.mtime = Instant.now();
    }

    // }}} getter and setter //

    // manage each dict {{{ //
    public void initializeTables() throws SQLException {
        db.getConnectionUseDbName();

        if (!hasTables()) {
            db.createTable(SQLStr.createTableWords(this.getShortName()));
            db.createTable(SQLStr.createTableFrequencies(this.getShortName()));
            db.createTable(SQLStr.createTableEntries(this.getShortName()));
            db.createTable(SQLStr.createTableExamples(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyFreId(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyWordId(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyEntryId(this.getShortName()));
        }

        Utils.info(this.getShortName() + " dictionary INITED");
    }

    public void dropTables() throws IOException, SQLException {
        db.getConnectionUseDbName();
        db.dropTable(SQLStr.dropTableInDict(this.getShortName()));

        Utils.info(this.getShortName() + " dictionary tables DELETED");
    }

    public boolean hasTables() throws SQLException {
        Boolean result = false;
        Connection con = db.getCurrentConUseDbName();

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.hasTables(this.getShortName());

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
                        this.getShortName() + "_" + designedTables.get(i));
            }
            result = existedTables.containsAll(designedTables);

        } catch (SQLException e) {
            Database.printSQLException(e);
        }
        if (result) {
            Utils.info(this.getShortName() + " dictionary tables existed");
        } else {
            Utils.info(this.getShortName() + " dictionary tables NOT existed");
        }

        return result;
        // }}} process the ResultSet //
    }

    // public boolean checkForeignKey(){}

    // }}} manage each dict //

    // operater in dictionary {{{ //
    // Query a word {{{ //
    public Word queryWord(String wordSpell) throws SQLException {
        Word word = null;
        Connection con = db.getCurrentConUseDbName();

        // query from locale database
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWord(this.getShortName(), wordSpell);

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);

            String source = null;
            Pronounce pron = null;
            Frequency fre = null;
            int acounter = -1;
            Instant mtime = null;
            Instant atime = null;
            ArrayList<String> forms = null;

            ArrayList<SenseEntry> senseEntryList = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                if (count == 0) {
                    wordSpell = rs.getString("word_spell");
                    source = rs.getString("word_source");
                    String soundmark = rs.getString("word_pron_soundmark");
                    String sound = rs.getString("word_pron_sound");
                    pron = new Pronounce(soundmark, sound);
                    String freBand = String.valueOf(rs.getInt("fre_band"));
                    String freDescription = rs.getString("fre_description");
                    fre = new Frequency(freBand, freDescription);
                    forms = Utils.convertStringToArrayList(
                            rs.getString("word_forms"));
                    acounter = rs.getInt("word_acounter");
                    mtime = rs.getTimestamp("word_mtime").toInstant();
                    atime = rs.getTimestamp("word_atime").toInstant();
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
                    source, acounter, mtime, atime);
            updateWordAccess(word);
        // }}} process the ResultSet //
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

        return word;
    };
    // }}} Query a word //

    public void updateWordAccess(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update access info of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordAccess(
                        this.getShortName(),
                        wordSpell,
                        word.getAcounter() + 1));
            if (affectedRow > 0) {
                Utils.info(String.format(
                            "Updated the access info of word(%s)",
                            wordSpell));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the access info of word(%s)",
                            wordSpell));
            }
        }
    }

    public void updateWordModify(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update modify info of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordModify(
                        this.getShortName(),
                        wordSpell));
            if (affectedRow > 0) {
                Utils.info(String.format(
                            "Updated the modify info of word(%s)",
                            wordSpell));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the modify info of word(%s)",
                            wordSpell));
            }
        }

    }

    // add a word {{{ //

    // set prepareStatement {{{ //
    private PreparedStatement setPstmtFrequencies(PreparedStatement pstmt,
            Word word) throws SQLException {
        pstmt.setInt(1, Integer.parseInt(word.getFrequency().getBand()));
        pstmt.setString(2, word.getFrequency().getDescription());

        return pstmt;
    }

    private PreparedStatement setPstmtWords(PreparedStatement pstmt, Word word, int freId)
            throws SQLException {
        pstmt.setString(1, word.getSpell());
        pstmt.setString(2, word.getSource());
        pstmt.setString(3, word.getForms().toString());
        pstmt.setString(4, word.getPronounce().getSoundmark());
        pstmt.setString(5, word.getPronounce().getSound());
        Instant atime =
            (word.getAtime() == null) ? Instant.now() : word.getAtime();
        Instant mtime =
            (word.getMtime() == null) ? Instant.now() : word.getMtime();
        pstmt.setTimestamp(6, Timestamp.from(atime));
        pstmt.setInt(7, word.getAcounter());
        pstmt.setTimestamp(8, Timestamp.from(mtime));
        pstmt.setInt(SQLStr.columnListInWords.length, freId);

        return pstmt;
    }

    private PreparedStatement setPstmtEntries(PreparedStatement pstmt,
            String entry_wordClass, String entry_sense, int wordId) throws SQLException {
        pstmt.setString(1, entry_wordClass);
        pstmt.setString(2, entry_sense);
        pstmt.setInt(SQLStr.columnListInEntries.length, wordId);

        return pstmt;
    };

    private PreparedStatement setPstmtExamples(PreparedStatement pstmt,
            String example_text, int entryId) throws SQLException {
        pstmt.setString(1, example_text);
        pstmt.setInt(SQLStr.columnListInExamples.length, entryId);

        return pstmt;
    };

    // }}} set prepareStatement //

    public void addWord(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't add an empty word to database.");
        } else {
            // initial variables {{{ //
            Connection con = db.getCurrentConUseDbName();
            PreparedStatement pstmtFrequencies = null;
            PreparedStatement pstmtWords = null;
            PreparedStatement pstmtEntries = null;
            PreparedStatement pstmtExamples = null;
            ResultSet rs = null;
            int affectedRow = 0;
            int freId = 0;
            int wordId = 0;
            int entryId = 0;
            // }}} initial variables //

            try {
                con.setAutoCommit(false);

                // initial prepareStatement {{{ //
                pstmtFrequencies = con.prepareStatement(
                        SQLStr.insertValueIntoFrequenies(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtWords = con.prepareStatement(
                        SQLStr.insertValueIntoWords(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtEntries = con.prepareStatement(
                        SQLStr.insertValueIntoEntries(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtExamples = con.prepareStatement(
                        SQLStr.insertValueIntoExamples(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                // }}} initial prepareStatement //

                // into frequencies table {{{ //
                pstmtFrequencies = setPstmtFrequencies(pstmtFrequencies, word);
                try {
                    affectedRow = pstmtFrequencies.executeUpdate();
                    rs = pstmtFrequencies.getGeneratedKeys();
                } catch(SQLException e) {
                    if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                        Utils.warning("Duplicated fre_band");
                        Utils.info("Querying freId from database ...");
                        Statement stmt = con.createStatement();
                        rs = stmt.executeQuery(
                                SQLStr.queryFreId(this.getShortName(),
                                    word.getFrequency().getBand()));
                    }
                }
                if (rs != null && !rs.isClosed() && rs.next()) {
                    freId = rs.getInt(1);
                    rs.close();
                }
                // }}} into frequencies tabl //

                if (freId > 0) {
                    // into words table {{{ //
                    pstmtWords = setPstmtWords(pstmtWords, word, freId);
                    try {
                        affectedRow = pstmtWords.executeUpdate();
                        rs = pstmtWords.getGeneratedKeys();
                    } catch(SQLException e) {
                    if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                        Utils.warning(
                            "Duplicated word_spell, please try update method");
                    }
                    }

                    if (rs != null && !rs.isClosed() && rs.next()) {
                        wordId = rs.getInt(1);
                        rs.close();
                    }
                    // }}} into words table //

                    if (wordId > 0 && affectedRow == 1) {
                        // into entries table {{{ //
                        for(SenseEntry entry : word.getSenesEntries()) {
                            pstmtEntries = setPstmtEntries(pstmtEntries,
                                    entry.getWordClass(), entry.getSense(), wordId);
                            affectedRow = pstmtEntries.executeUpdate();
                            rs = pstmtEntries.getGeneratedKeys();
                            if (rs != null && !rs.isClosed() && rs.next()) {
                                entryId = rs.getInt(1);
                                rs.close();
                            }
                        // }}} into entries table //

                            // into examples table {{{ //
                            if (entryId > 0 && affectedRow == 1) {
                                for(String example : entry.getExamples()) {
                                    pstmtExamples = setPstmtExamples(
                                            pstmtExamples, example, entryId);
                                    pstmtExamples.executeUpdate();
                                }
                            }
                            // }}} into examples tabl //
                        }
                    }
                }

                con.commit();
                Utils.info("Added a word (" + word.getSpell() + ") to {"
                        + this.getName() + "} database");
            } catch (SQLException e) {
                // rollback {{{ //
                try {
                    if (con != null)
                        con.rollback();
                } catch (SQLException ex) {
                    Database.printSQLException(ex);
                }
                Database.printSQLException(e);
                // }}} rollback //
            } finally {
                // finally close everything{{{ //
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
                } catch (SQLException e) {
                    Database.printSQLException(e);
                }
                // }}} finally close everything //
            }
        }
    }
    // }}} add a word //

    // delete a word {{{ //
    public void deleteWord(String wordSpell) throws SQLException {
        Connection con = db.getCurrentConUseDbName();
        int affectedRow = 0;

        // delete word from locale database by wordSpell
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.deleteWord(this.getShortName(), wordSpell);
            affectedRow = stmt.executeUpdate(query);

            if(affectedRow > 0) {
                Utils.info(
                        String.format("Deleted word (%s) from {%s} database",
                                wordSpell, this.getName()));
            } else {
                Utils.warning(String.format(
                        "There is no word (%s) in {%s} database. "
                                + "Please check word spell.",
                        wordSpell, this.getName()));
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

    };
    // }}} delete a word //

    // update {{{ //
    public void updateWord(Word word) throws SQLException {
        if (!word.isEmypty()) {

            Utils.info(String.format(
                    "==> Trying to update word (%s) in {%s} database...",
                    word.getSpell(), this.getName()));

            Word oldWord = queryWord(word.getSpell());
            if (!oldWord.isEmypty()) {
                Utils.debug("oldWord acounter: " + oldWord.getAcounter());
                Utils.debug("oldWord atime: " + oldWord.getAtime());

                word.setAtime(oldWord.getAtime());
                word.setAcounter(oldWord.getAcounter());
                deleteWord(oldWord.getSpell());
            }

            addWord(word);
            Utils.info(String.format(
                    "<== Updated word (%s) in {%s} database",
                    word.getSpell(), this.getName()));
            updateWordModify(word);
        }
    }
    // }}} update //

    // size {{{ //
    public int size() throws SQLException {
        Connection con = db.getCurrentConUseDbName();
        int size = 0;

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.querySize(this.getShortName());

            ResultSet rs = stmt.executeQuery(query);
            if (rs.next())
                size = rs.getInt(1);

            Utils.info(String.format(
                        "{%s} database included %d words.",
                        this.getName(), size));
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
        return size;
    }
    // }}} size //



    // }}} operater in dictionary //

    // abstract Word random();
    abstract void build();
}
