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
            db.createTable(SQLStr.createTableRefWordsFrequencies(
                        this.getShortName()));

            db.addForeignKey(SQLStr.addForeignKeyWordId(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyEntryId(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyRefFreId(this.getShortName()));
            db.addForeignKey(SQLStr.addForeignKeyRefWordId(this.getShortName()));
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

    // make words from result set {{{ //
    private static ArrayList<ResultRowQueryWord> convertQueryResultSetToArray (ResultSet rs)
        throws SQLException{
        ArrayList<ResultRowQueryWord> wordRows = new ArrayList<>();
        while(rs.next()) {
            ResultRowQueryWord row = new ResultRowQueryWord(rs);
            wordRows.add(row);
        }

        return wordRows;
    }

    private static ArrayList<Integer> getWordIds(ArrayList<ResultRowQueryWord> wordRows) {
        ArrayList<Integer> wordIds = new ArrayList<>();
        for(ResultRowQueryWord row : wordRows) {
            if(! wordIds.contains(row.word_id)) {
                wordIds.add(row.word_id);
            }
        }

        return wordIds;
    }

    public static ArrayList<Word> makeWordsFromResultSet(ResultSet rs)
            throws SQLException {
        ArrayList<Word> words = new ArrayList<>();
        ArrayList<ResultRowQueryWord> rows = convertQueryResultSetToArray(rs);
        ArrayList<Integer> wordIds = getWordIds(rows);

        for(int wordId : wordIds) {
            String spell = null;
            String source = null;
            Pronounce pron = new Pronounce();
            ArrayList<String> forms = null;
            ArrayList<SenseEntry> senseEntryList = new ArrayList<>();
            ArrayList<Frequency> freList = new ArrayList<>();
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
                        forms = Utils.convertStringToArrayList(row.word_forms);
                        acounter = row.word_acounter;
                        mtime = row.word_mtime.toInstant();
                        atime = row.word_atime.toInstant();

                        newWordFlag = false;
                    }
                    Frequency fre = new Frequency();
                    fre.setBand(row.fre_band);
                    fre.setDescription(row.fre_description);
                    if (freList.contains(fre)) {
                        freList.add(fre);
                    }
                    SenseEntry senseEntry = new SenseEntry();
                    senseEntry.setWordClass(row.entry_wordClass);
                    senseEntry.setSense(row.entry_sense);
                    senseEntry.addExample(row.example_text);
                    senseEntryList.add(senseEntry);
                }
            }
            senseEntryList = SenseEntry.noDuplicatedSense(senseEntryList);

            Word word = new Word(spell, pron, freList, forms, senseEntryList,
                    source, acounter, mtime, atime);
            words.add(word);
        }

        return words;
    }
    // }}} make words from result set //

    // Query a word {{{ //
    public Word queryWord(String wordSpell, String wordSource)
            throws SQLException {
        Word word = null;
        Connection con = db.getCurrentConUseDbName();

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWordBySpellAndSource(
                    this.getShortName(), wordSpell, wordSource);
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<Word> words = makeWordsFromResultSet(rs);
            if(words.size() > 0) word = words.get(0);
            if(LocalDict.updateWordAccess) updateWordAccess(word);
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

        return word;
    }

    public ArrayList<Word> queryWord(String wordSpell)
            throws SQLException {
        ArrayList<Word> words = new ArrayList<>();
        Connection con = db.getCurrentConUseDbName();

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.queryWordBySpell(this.getShortName(), wordSpell);
            ResultSet rs = stmt.executeQuery(query);
            words = makeWordsFromResultSet(rs);
            for(Word word : words) {
                if (LocalDict.updateWordAccess) updateWordAccess(word);
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }

        return words;
    }
    // }}} Query a word //

    // update word fields {{{ //
    // update atime and acounter {{{ //
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
                            "Updated the access info by spell(%s) && source(%s)",
                            wordSpell,
                            wordSource));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the access info by spell(%s) && source(%s)",
                            wordSpell,
                            wordSource));
            }
        }
    }
    // }}} update atime and acounter//
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
                            "Updated the modify info by spell(%s) && source(%s)",
                            wordSpell,
                            wordSource));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the modify info by spell(%s) && source(%s)",
                            wordSpell,
                            wordSource));
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
                Utils.info(String.format("Updated the forms<%s> by spell(%s) && source(%s)",
                        wordForms.toString(), wordSpell, wordSource));
            } else {
                Utils.warning(String.format(
                        "Couldn't Update the forms<%s> by spell(%s) && source(%s)",
                        wordForms.toString(), wordSpell, wordSource));
            }
        }
    }

    // }}} update word forms //
    // update word frequency {{{ //
    public void updateWordFrequency(Word word, ArrayList<Frequency> freList)
            throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update frequency List of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            word.setFrequencies(freList);
            Connection con = db.getCurrentConUseDbName();

            try {
                con.setAutoCommit(false);
                ArrayList<Integer> freIdList = insertValueIntoFrequenies(con, word);
                int wordId = queryWordId(con, word);
                if(freIdList.size() > 0) {
                    insertValueIntoRefWordsFres(con, wordId, freIdList);
                }
                con.commit();
                Utils.info(String.format("Updated the freList<%d> by spell(%s) && source(%s)",
                        freList.size(), wordSpell, wordSource));
            } catch (SQLException e) {
                Utils.warning(String.format("Couldn't update the freList by spell(%s) && source(%s)",
                        wordSpell, wordSource));
                rollback(con);
                Database.printSQLException(e);
            } finally {
                // finally close everything{{{ //
                try {
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
                            "Updated the pronounce<%s> by spell(%s) && source(%s)",
                            pronounce.toString(),
                            wordSpell,
                            wordSource));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the pronounce<%s> by spell(%s) && source(%s)",
                            pronounce.toString(),
                            wordSpell,
                            wordSource));
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
                            "Updated the newSource<%s> by spell(%s) && source(%s)",
                            newWordSource,
                            wordSpell,
                            oldWordSource));
            } else {
                Utils.warning(String.format(
                            "Couldn't Update the newSource<%s> by spell(%s) && source(%s)",
                            newWordSource,
                            wordSpell,
                            oldWordSource));
            }
        }
    }

    // }}} update word source //
    // update word senseEntryList {{{ //
    public int queryWordId(String wordSpell, String wordSource)
            throws SQLException {
        int wordId = 0;
        Connection con = db.getCurrentConUseDbName();
        wordId = queryWordId(con, wordSpell, wordSource);

        return wordId;
    }

    public int queryWordId(Connection con,
            String wordSpell, String wordSource) throws SQLException {
        int wordId = 0;
        ResultSet rs = null;

        try (Statement stmt = con.createStatement()){
            rs = stmt.executeQuery(SQLStr.queryWordId(
                    this.getShortName(), wordSpell, wordSource));
            if (rs != null && !rs.isClosed() && rs.next()) {
                wordId = rs.getInt(1);

                if ( wordId == 0) {
                Utils.warning(
                    String.format("Couldn't found wordId by spell(%s) && source(%s)\n",
                        wordSpell, wordSource));
                }
                rs.close();
            }
        } catch(SQLException e) {
            Database.printSQLException(e);
            rollback(con);
        }

        return wordId;
    }

    public int queryWordId(Word word) throws SQLException {
        String wordSpell = word.getSpell();
        String wordSource = word.getSource();

        return queryWordId(wordSpell, wordSource);
    }

    public int queryWordId(Connection con, Word word) throws SQLException {
        String wordSpell = word.getSpell();
        String wordSource = word.getSource();

        return queryWordId(con, wordSpell, wordSource);
    }

    public void deleteWordSenseEntries(int wordId) throws SQLException{
        Connection con = db.getCurrentConUseDbName();
        deleteWordSenseEntries(con, wordId);
    }

    public void deleteWordSenseEntries(Connection con, int wordId)
            throws SQLException{
        int affectedRow = 0;
        try (Statement stmt = con.createStatement();){
            affectedRow = stmt.executeUpdate(
                SQLStr.deleteWordSenseEntries(this.getShortName(),
                wordId));
            if (affectedRow > 0) {
                Utils.info(String.format("Deleted %d senseEntries by wordId(%d)",
                            affectedRow, wordId));
            } else {
                Utils.warning(String.format(
                            "Couldn't find any senseEntries by wordId(%d)",
                            wordId));
            }
        } catch(SQLException e) {
            Database.printSQLException(e);
            rollback(con);
        }
    }

    public void deleteWordSenseEntries(Connection con,
            String wordSpell, String wordSource) throws SQLException{
        int affectedRow = 0;
        try (Statement stmt = con.createStatement();){
            affectedRow = stmt.executeUpdate(
                SQLStr.deleteWordSenseEntries(this.getShortName(),
                wordSpell, wordSource));
            if (affectedRow > 0) {
                Utils.info(String.format(
                "Deleted %d senseEntries by spell(%s) && source(%s)",
                affectedRow, wordSpell, wordSource));
            } else {
                Utils.warning(String.format(
                "Couldn't find any senseEntries by spell(%s) && source(%s)",
                wordSpell, wordSource));
            }
        } catch(SQLException e) {
            Database.printSQLException(e);
            rollback(con);
        }
    }

    public void updateWordSenseEntries(Word word,
            ArrayList<SenseEntry> wordSenseEntryList, boolean appendMode)
            throws SQLException {
        if (word.isEmypty()) {
            Utils.warning("Couldn't update senseEntryies of an empty word.");
        } else {
            String wordSpell = word.getSpell();
            String wordSource = word.getSource();
            Connection con = db.getCurrentConUseDbName();
            int wordId = 0;

            try {
                con.setAutoCommit(false);
                wordId = queryWordId(con, word);
                if (wordId > 0) {
                    if(!appendMode) {
                        deleteWordSenseEntries(con, wordId);
                    }
                    word.setSenseEntries(wordSenseEntryList);
                    insertValueIntoEntries(con, word, wordId);
                }
                con.commit();
            Utils.info(String.format(
                        "Updated the senseEntries by spell(%s) && source(%s)",
                        wordSpell, wordSource));
            } catch (SQLException e) {
                // rollback {{{ //
                Utils.warning(String.format(
                            "Couldn't updated the senseEntries by spell(%s) && source(%s)",
                            wordSpell, wordSource));
                Database.printSQLException(e);
                rollback(con);
                // }}} rollback //
            } finally {
                // finally close everything{{{ //
                try {
                    if (con != null)
                        con.setAutoCommit(true);
                } catch (SQLException e) {
                    Database.printSQLException(e);
                }
                // }}} finally close everything //
            }
        }
    }
    // }}} update word senseEntryList //
    // }}} update word fields //

    // add a word {{{ //

    // set prepareStatement {{{ //
    private PreparedStatement setPstmtFrequencies(Connection con,
            Frequency fre) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
                SQLStr.insertValueIntoFrequenies(this.getShortName()),
                Statement.RETURN_GENERATED_KEYS);

        String band = fre.getBand();
        pstmt.setString(1, band);
        pstmt.setString(2, fre.getDescription());

        return pstmt;
    }

    private PreparedStatement setPstmtWords(Connection con, Word word)
            throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
        SQLStr.insertValueIntoWords(this.getShortName()),
        Statement.RETURN_GENERATED_KEYS);

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

        return pstmt;
    }

    private PreparedStatement setPstmtEntries(Connection con,
            String entry_wordClass, String entry_sense, int wordId) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
                SQLStr.insertValueIntoEntries(this.getShortName()),
                Statement.RETURN_GENERATED_KEYS);

        pstmt.setString(1, entry_wordClass);
        pstmt.setString(2, entry_sense);
        pstmt.setInt(SQLStr.columnListInEntries.length, wordId);

        return pstmt;
    };

    private PreparedStatement setPstmtExamples(Connection con,
            String example_text, int entryId) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
                SQLStr.insertValueIntoExamples(this.getShortName()),
                Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, example_text);
        pstmt.setInt(SQLStr.columnListInExamples.length, entryId);

        return pstmt;
    };

    private PreparedStatement setPstmtRefWordsFres(Connection con,
            int wordId, int freId) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
        SQLStr.insertValueIntoRefWordsFres(this.getShortName()),
        Statement.RETURN_GENERATED_KEYS);

        pstmt.setInt(1, wordId);
        pstmt.setInt(2, freId);

        return pstmt;
    };
    // }}} set prepareStatement //

    // insert value into localdict tables {{{ //
    private ArrayList<Integer> insertValueIntoFrequenies(Connection con,
            Word word)
            throws SQLException {
        ResultSet rs = null;
        ArrayList<Integer> freIdList = new ArrayList<>();

        for(Frequency fre : word.getFrequencies()) {
            try(PreparedStatement pstmtFrequencies = setPstmtFrequencies(con, fre);) {
                rs = pstmtFrequencies.getGeneratedKeys();
                if (rs != null && !rs.isClosed() && rs.next()) {
                    int freId = rs.getInt(1);
                    Utils.debug("freId: " + freId);
                    freIdList.add(freId);
                    rs.close();
                }
            } catch(SQLException e) {
                if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                    Utils.warning("Duplicated fre_band");
                    Utils.info("Querying freId from database ...");
                } else {
                    rollback(con);
                    Database.printSQLException(e);
                }
            }
        }

        return freIdList;
    }

    private int insertValueIntoWords(Connection con, Word word)
            throws SQLException{
        int affectedRow = 0;
        int wordId = 0;
        ResultSet rs = null;

        try(PreparedStatement pstmtWords = setPstmtWords(con, word);) {
            affectedRow = pstmtWords.executeUpdate();
            rs = pstmtWords.getGeneratedKeys();
        } catch(SQLException e) {
            if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                Utils.warning(
            "Duplicated (word_spell, word_source), please try update method");
            } else {
                rollback(con);
                Database.printSQLException(e);
            }
        }

        if (affectedRow > 0 && rs != null && !rs.isClosed() && rs.next()) {
            wordId = rs.getInt(1);
            rs.close();
        }

        return wordId;
    }

    private void insertValueIntoEntries(Connection con, Word word, int wordId)
        throws SQLException {
        int affectedRow = 0;
        ResultSet rs = null;
        int entryId = 0;

        for(SenseEntry entry : word.getSenseEntries()) {
            try (PreparedStatement pstmtEntries = setPstmtEntries(con,
                    entry.getWordClass(), entry.getSense(), wordId);) {
                affectedRow = pstmtEntries.executeUpdate();
                rs = pstmtEntries.getGeneratedKeys();
                if (rs != null && !rs.isClosed() && rs.next()) {
                    entryId = rs.getInt(1);
                    rs.close();
                }

                if (entryId > 0 && affectedRow == 1) {
                    insertValueIntoExamples(con, entry, entryId);
                }
            } catch(SQLException e) {
                rollback(con);
                Database.printSQLException(e);
            }
        }
    }

    private void insertValueIntoExamples(Connection con,
            SenseEntry entry, int entryId) throws SQLException {

        for(String example : entry.getExamples()) {
            try(PreparedStatement pstmtExamples = setPstmtExamples(con, example, entryId);) {
                pstmtExamples.executeUpdate();
            } catch(SQLException e) {
                rollback(con);
                Database.printSQLException(e);
            }
        }
    }

    private void insertValueIntoRefWordsFres(Connection con,
            int wordId, ArrayList<Integer> freIdList) throws SQLException {

        for(int freId : freIdList) {
            try (PreparedStatement pstmtRefWordsFres = setPstmtRefWordsFres(con, wordId, freId);) {
                pstmtRefWordsFres.executeUpdate();
            } catch(SQLException e) {
                rollback(con);
                Database.printSQLException(e);
            }
        }
    }
    // }}} insert value into localdict tables //

    private void rollback(Connection con) {
        try {
            if (con != null)
                con.rollback();
        } catch (SQLException ex) {
            Database.printSQLException(ex);
        }
    }

    public int addWord(Word word) throws SQLException {
        int wordId = 0;
        if (word.isEmypty()) {
            Utils.warning("Couldn't add an empty word to database.");
        } else {
            Connection con = db.getCurrentConUseDbName();

            try {
                con.setAutoCommit(false);
                ArrayList<Integer> freIdList = insertValueIntoFrequenies(con, word);
                wordId = insertValueIntoWords(con, word);
                if (wordId > 0) {
                    insertValueIntoEntries(con, word, wordId);
                    if(freIdList.size() > 0) {
                        insertValueIntoRefWordsFres(con, wordId, freIdList);
                    }
                }
                con.commit();
                Utils.info("Added a word (" + word.getSpell() + ") to {"
                        + this.getName() + "} database");
            } catch (SQLException e) {
                // rollback {{{ //
                rollback(con);
                Database.printSQLException(e);
                // }}} rollback //
            } finally {
                // finally close everything{{{ //
                try {
                    if (con != null)
                        con.setAutoCommit(true);
                } catch (SQLException e) {
                    Database.printSQLException(e);
                }
                // }}} finally close everything //
            }
        }
        return wordId;
    }
    // }}} add a word //

    // delete a word {{{ //
    public void deleteWord(String wordSpell) throws SQLException {
        Connection con = db.getCurrentConUseDbName();
        int affectedRow = 0;

        // delete word from locale database by wordSpell
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.deleteWordBySpell(this.getShortName(), wordSpell);
            affectedRow = stmt.executeUpdate(query);

            if(affectedRow > 0) {
                Utils.info(
                    String.format("Deleted word (%s) from {%s} database by spell, deleted %d words",
                                wordSpell, this.getName(), affectedRow));
            } else {
                Utils.warning(String.format(
                        "Nothing matched  spell(%s) in {%s} database. "
                                + "Please check word spell.",
                        wordSpell, this.getName()));
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    };

    public void deleteWord(String wordSpell, String wordSource) throws SQLException {
        Connection con = db.getCurrentConUseDbName();
        int affectedRow = 0;

        // delete word from locale database by wordSpell
        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.deleteWordBySpellAndSource(
                    this.getShortName(), wordSpell, wordSource);
            affectedRow = stmt.executeUpdate(query);
            if(affectedRow > 0) {
                Utils.info(
                    String.format("Deleted word (%s) from {%s} database by spell and source, deleted %d words",
                                wordSpell, this.getName(), affectedRow));
            } else {
                Utils.warning(String.format(
                        "Nothing matched  spell(%s) && source(%s) in {%s} database. "
                                + "Please check word spell.",
                        wordSpell, wordSource, this.getName()));
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    };

    // }}} delete a word //

    // update {{{ //
    public void updateWord(Word newWord) throws SQLException {
        if (!newWord.isEmypty()) {
            Utils.info(String.format(
                    "==> Trying to update spell(%s) && source(%s) in {%s} database...",
                    newWord.getSpell(), newWord.getSource(), this.getName()));

            Word oldWord = queryWord(newWord.getSpell(), newWord.getSource());
            if (!oldWord.isEmypty()) {
                Utils.debug("oldWord acounter: " + oldWord.getAcounter());
                Utils.debug("oldWord atime: " + oldWord.getAtime());

                // Reserve the access time and Acounter of the oldWord
                newWord.setAtime(oldWord.getAtime());
                newWord.setAcounter(oldWord.getAcounter());
                deleteWord(oldWord.getSpell(), oldWord.getSource());
            }

            int newWordId = addWord(newWord);
            Utils.info(String.format(
                    "<== Updated spell(%s) && source(%s) in {%s} database, new wordId(%d)",
                    newWord.getSpell(),newWord.getSource(),
                    this.getName(), newWordId));
            updateWordModify(newWord);
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

// Result Row {{{ //
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
// }}} Result Row //
