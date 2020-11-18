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
    private static boolean updateWordAccess = Utils.testString(
            App.configs.getProperty("updateWordAccess"));

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

    public Word queryWordBySpellAndSource(String wordSpell, String wordSource)
            throws SQLException {
        Word word = null;
        Connection con = db.getCurrentConUseDbName();

        // query from locale database
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWordBySpellAndSource(this.getShortName(), wordSpell, wordSource);

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
                    String freBand = rs.getString("fre_band");
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
            if (LocalDict.updateWordAccess && !word.isEmypty())
                updateWordAccess(word);
        // }}} process the ResultSet //
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

        return word;
    }

    public static ArrayList<Word> makeWordsFromResultSet(ResultSet rs)
            throws SQLException {
        ArrayList<Word> words = new ArrayList<>();
        ArrayList<ResultRowQueryWord> rows = new ArrayList<>();

        while(rs.next()) {
            ResultRowQueryWord row = new ResultRowQueryWord(rs);
            rows.add(row);
        }

        ArrayList<Integer> wordIds = new ArrayList<>();
        for(ResultRowQueryWord row : rows) {
            if(! wordIds.contains(row.word_id)) {
                wordIds.add(row.word_id);
            }
        }

        for(int wordId : wordIds) {
            String spell = null;
            String source = null;
            Pronounce pron = new Pronounce();
            Frequency fre = new Frequency();
            ArrayList<String> forms = null;
            ArrayList<SenseEntry> senseEntryList = new ArrayList<>();
            int acounter = 0;
            Instant mtime = null;
            Instant atime = null;

            boolean newWordFlag = true;

            for (ResultRowQueryWord row : rows) {
                if (row.word_id == wordId) {
                    if(newWordFlag) {
                        spell = row.word_spell;
                        source = row.word_source;
                        pron.setSoundmark(row.word_pron_soundmark);
                        pron.setSound(row.word_pron_sound);
                        fre.setBand(row.fre_band);
                        fre.setDescription(row.fre_description);
                        forms = Utils.convertStringToArrayList(row.word_forms);
                        acounter = row.word_acounter;
                        mtime = row.word_mtime.toInstant();
                        atime = row.word_atime.toInstant();

                        newWordFlag = false;
                    }
                    SenseEntry senseEntry = new SenseEntry();
                    senseEntry.setWordClass(row.entry_wordClass);
                    senseEntry.setSense(row.entry_sense);
                    senseEntry.addExample(row.example_text);

                    senseEntryList.add(senseEntry);
                }
            }
            senseEntryList = SenseEntry.noDuplicatedSense(senseEntryList);

            Word word = new Word(spell, pron, fre, forms, senseEntryList,
                    source, acounter, mtime, atime);
            words.add(word);
        }

        return words;
    }

    public ArrayList<Word> queryWordBySpell(String wordSpell)
            throws SQLException {
        ArrayList<Word> words = new ArrayList<>();
        Connection con = db.getCurrentConUseDbName();

        // query from locale database
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWordBySpell(this.getShortName(), wordSpell);

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);
            words = makeWordsFromResultSet(rs);

            for(Word word : words) {
                if (LocalDict.updateWordAccess && !word.isEmypty())
                    updateWordAccess(word);
            }
        // }}} process the ResultSet //
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

        return words;
    }
    // }}} Query a word //

    // update word fields {{{ //
    // update atime {{{ //
    public void updateWordAccess(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update access info of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordAccess(
                        this.getShortName(),
                        wordSpell,
                        wordSource,
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
    // }}} update atime //
    // update mtime {{{ //
    public void updateWordModify(Word word) throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update modify info of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordModify(
                        this.getShortName(),
                        wordSpell,
                        wordSource));
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
    // }}} update mtime //
    // update word forms {{{ //
    public void updateWordForms(Word word, ArrayList<String> wordForms)
            throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update forms of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(SQLStr.updateWordForms(
                    this.getShortName(), wordSpell, wordSource, wordForms));
            if (affectedRow > 0) {
                Utils.info(String.format("Updated the forms<%s> of word(%s)",
                        wordForms.toString(), wordSpell));
            } else {
                Utils.warning(String.format(
                        "Couldn't Update the forms<%s> of word(%s)",
                        wordForms.toString(), wordSpell));
            }
        }
    }

    // }}} update word forms //
    // update word frequency {{{ //
    public void updateWordFrequency(Word word, Frequency wordFrequency)
            throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update frequency of an empty word.");
        } else {
            // initialize {{{ //
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            PreparedStatement pstmtFrequencies = null;
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            int freId = 0;
            // }}} initialize //

            try {
                con.setAutoCommit(false);
                pstmtFrequencies = con.prepareStatement(
                        SQLStr.insertValueIntoFrequenies(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                // try to get fre_id {{{ //
                pstmtFrequencies = setPstmtFrequencies(pstmtFrequencies, word);
                try {
                    affectedRow = pstmtFrequencies.executeUpdate();
                    rs = pstmtFrequencies.getGeneratedKeys();
                } catch (SQLException e) {

                    if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                        Utils.warning("Duplicated fre_band");
                        Utils.info("Querying freId from database ...");
                        rs = stmt.executeQuery(SQLStr.queryFreId(
                                this.getShortName(), wordFrequency.getBand()));
                    }
                }

                if (rs != null && !rs.isClosed() && rs.next()) {
                    freId = rs.getInt(1);
                    rs.close();
                }
                // }}} try to get fre_id //
                if (freId > 0) {

                    // update fre_id in words table {{{ //
                    affectedRow = stmt.executeUpdate(SQLStr.updateWordFreID(
                            this.getShortName(), wordSpell, wordSource, freId));
                    if (affectedRow > 0) {
                        Utils.info(String.format(
                                "Updated the frequency<%s> of word(%s)",
                                wordFrequency.toString(), wordSpell));
                    } else {
                        Utils.warning(String.format(
                                "Couldn't Update the frequency<%s> of word(%s)",
                                wordFrequency.toString(), wordSpell));
                    }
                    // }}} update fre_id in words table //
                }
                con.commit();

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
                    if (con != null)
                        con.setAutoCommit(true);
                } catch (SQLException e) {
                    Database.printSQLException(e);
                }
                // }}} finally close everything //
            }
        }
    }

    // }}} update word frequency //
    // update word pronounce {{{ //
        public void updateWordPronounce(Word word, Pronounce pronounce)
                throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update pronounce of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordPronounce(
                        this.getShortName(),
                        wordSpell,
                        wordSource,
                        pronounce));
            if (affectedRow > 0) {
                Utils.info(String.format(
                            "Updated the pronounce<%s> of word(%s)",
                            pronounce.toString(),
                            wordSpell));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the pronounce<%s> of word(%s)",
                            pronounce.toString(),
                            wordSpell));
            }
        }
    }

    // }}} update word pronounce //
    // update word source {{{ //
        public void updateWordSource(Word word, String newWordSource)
                throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update source of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String oldWordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            Statement stmt = con.createStatement();
            affectedRow = stmt.executeUpdate(
                    SQLStr.updateWordSource(
                        this.getShortName(),
                        wordSpell,
                        oldWordSource,
                        newWordSource));
            if (affectedRow > 0) {
                Utils.info(String.format(
                            "Updated the source<%s> of word(%s)",
                            newWordSource,
                            wordSpell));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the source<%s> of word(%s)",
                            newWordSource,
                            wordSpell));
            }
        }
    }

    // }}} update word source //
    // update word senseEntryList {{{ //
    public void updateWordSenseEntries(Word word,
            ArrayList<SenseEntry> wordSenseEntryList, boolean appendMode)
            throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update senseEntryies of an empty word.");
        } else {
            // initialize {{{ //
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            int affectedRow = 0;
            Connection con = db.getCurrentConUseDbName();
            PreparedStatement pstmtEntries = null;
            PreparedStatement pstmtExamples = null;
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            int wordId = 0;
            int entryId = 0;
            // }}} initialize //

            try {
                con.setAutoCommit(false);
                pstmtEntries = con.prepareStatement(
                        SQLStr.insertValueIntoEntries(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                pstmtExamples = con.prepareStatement(
                        SQLStr.insertValueIntoExamples(this.getShortName()),
                        Statement.RETURN_GENERATED_KEYS);
                // try to get word_id {{{ //
                rs = stmt.executeQuery(SQLStr.queryWordId(
                        this.getShortName(), wordSpell, wordSource));
                if (rs != null && !rs.isClosed() && rs.next()) {
                    wordId = rs.getInt(1);
                    rs.close();
                }

                // }}} try to get word_id //
                if (wordId > 0) {
                    // delete old senseEntries from database {{{ //
                    if(!appendMode) {
                        affectedRow = stmt.executeUpdate(
                            SQLStr.deleteWordSenseEntries(this.getShortName(),
                            wordId));
                        if(affectedRow > 0) {
                            Utils.info(String.format(
                                "Deleted all %d senseEntries of word(%s)",
                                affectedRow, wordSpell));
                            affectedRow = 0;
                        } else {
                            Utils.warning(String.format(
                                "Couldn't deleted any senseEntries of word(%s)",
                                affectedRow, wordSpell));
                        }
                    }
                    // }}} delete old senseEntries from database //
                    // into entries table {{{ //
                    for(SenseEntry entry : wordSenseEntryList) {
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
                        // }}} into examples table //
                }
                }

            con.commit();

            Utils.info(String.format(
                        "Updated the senseEntries of word(%s)",
                        wordSpell));

            } catch (SQLException e) {
                // rollback {{{ //
                Utils.warning(String.format(
                            "Couldn't updated the senseEntries of word(%s)",
                            wordSpell));
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
            PreparedStatement pstmtEntries = null;
            PreparedStatement pstmtExamples = null;
    // }}} update word senseEntryList //
    // }}} update word fields //

    // add a word {{{ //

    // set prepareStatement {{{ //
    private PreparedStatement setPstmtFrequencies(PreparedStatement pstmt,
            Word word) throws SQLException {

        String band = word.getFrequency().getBand();
        pstmt.setString(1, band);
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
                    } else {
                        Database.printSQLException(e);
                    }
                }
                if (rs != null && !rs.isClosed() && rs.next()) {
                    freId = rs.getInt(1);
                    Utils.debug("freId: " + freId);
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
                                "Duplicated (word_spell, word_source), please try update method");
                        } else {
                            Database.printSQLException(e);
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
                            // }}} into examples table //
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

            Word oldWord = queryWordBySpellAndSource(word.getSpell(),
                    word.getSource());
            if (!oldWord.isEmypty()) {
                Utils.debug("oldWord acounter: " + oldWord.getAcounter());
                Utils.debug("oldWord atime: " + oldWord.getAtime());

                // Reserve the access time and Acounter of the oldWord
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

/**
 * ResultRowQueryWord
 */
class ResultRowQueryWord {
    public int word_id;
    public String word_spell;
    public String word_source;
    public String word_forms;
    public String word_pron_soundmark;
    public String word_pron_sound;
    public String fre_band;
    public String fre_description;
    public int word_acounter;
    public Timestamp word_mtime;
    public Timestamp word_atime;
    // public int entry_id;
    public String entry_wordClass;
    public String entry_sense;
    public String example_text;

    public ResultRowQueryWord(ResultSet rs) throws SQLException {
        this.word_id = rs.getInt("word_id");
        this.word_spell = rs.getString("word_spell");
        this.word_source = rs.getString("word_source");
        this.word_pron_soundmark = rs.getString("word_pron_soundmark");
        this.word_pron_sound = rs.getString("word_pron_sound");
        this.word_forms = rs.getString("word_forms");
        this.fre_band = rs.getString("fre_band");
        this.fre_description = rs.getString("fre_description");
        this.word_acounter = rs.getInt("word_acounter");
        this.word_mtime = rs.getTimestamp("word_mtime");
        this.word_atime = rs.getTimestamp("word_atime");
        // this.entry_id = rs.getInt("entry_id");
        this.entry_wordClass = rs.getString("entry_wordClass");
        this.entry_sense = rs.getString("entry_sense");
        this.example_text = rs.getString("example_text");
    }

    // public int get_word_id() {
    //     return this.word_id;
    // }
    // public String get_word_spell() {
    //     return this.word_spell;
    // }
    // public String get_word_source() {
    //     return this.word_source;
    // }
    // public String get_word_forms() {
    //     return this.word_forms;
    // }
    // public String get_word_pron_soundmark() {
    //     return this.word_pron_soundmark;
    // }
    // public String get_word_pron_sound() {
    //     return this.word_pron_sound;
    // }
    // public String get_fre_band() {
    //     return this.fre_band;
    // }
    // public String get_fre_description() {
    //     return this.fre_description;
    // }
    // public int get_word_acounter() {
    //     return this.word_acounter;
    // }
    // public Timestamp get_word_mtime() {
    //     return this.word_mtime;
    // }
    // public Timestamp get_word_atime() {
    //     return this.word_atime;
    // }
    // public int get_entry_id() {
    //     return this.entry_id;
    // }
    // public String get_entry_wordClass() {
    //     return this.entry_wordClass;
    // }
    // public String get_entry_sense() {
    //     return this.entry_sense;
    // }
    // public String get_example_text() {
    //     return this.example_text;
    // }
}
