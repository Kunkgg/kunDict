/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package kunDict;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM)
public class AppTest {
    public App app;
    public static boolean testFlag = false;

    public AppTest() throws IOException, SQLException{
        this.app = new App();
    }

    // App database {{{ //
    // @Ignore
    // @Test
    // public void testAppInitializeTables() throws IOException, SQLException {
    //     app.initializeTables();
    //     assertTrue("Should have tables", app.hasTables());
    // }

    // @Test
    // public void testAppHasTables() throws IOException, SQLException {
    //     assertTrue("Should have tables", app.hasTables());
    // }


    // @Test
    // public void testAppInsertValuesIntoDictTypes()
    //     throws IOException, SQLException {
    //     app.insertValuesIntoDictTypes();
    //     // assertTrue("Should have tables", app.hasTables());
    // }
    // }}} App database //

    // Request {{{ //
    @Ignore
    @Test
    public void testRequest() throws URISyntaxException {
        String url = "https://www.collinsdictionary.com/us/dictionary/english/water";
        String fileName = "water.html";
        Request req = new Request(url);
        req.setBodyHandler(HttpResponse.BodyHandlers.ofFile(Paths.get(fileName)));
        HttpResponse<String> response = req.get();

        // System.out.println(response.body().substring(0, 500));
        assertEquals(200, response.statusCode());
        assertNotNull("request should get response", response);
    }
    // }}} Request //

    // Extractor {{{ //
    @Ignore
    @Test
    public void testCollinsExtracotr() throws IOException {
        String fileName = "water.html";
        String html = Files.readString(Path.of(fileName));
        Extractor extractor = new Extractor(html);
        Word water = extractor.collinsOnline();
        assertNotNull("water should be a Word class type", water);
        assertEquals("should be water", "water", water.getSpell());
        assertEquals("frequency should be 5", "frequency band: 5",
                water.getFrequency().toString());
        assertEquals("sensEntryList size should be 5", 5,
                water.getSenesEntries().size());
        assertNotNull("sensEntryList should not Null", water.getSenesEntries());
    }
    // }}} Extractor //

    // CollinsQuery {{{ //
    @Ignore
    @Test
    public void testCollinsQuery() throws IOException {
        CollinsOnlineDict collins = new CollinsOnlineDict();
        assertEquals("Should get name of dictionary",
                "Collins Online English Dictionary", collins.getName());

        // words {{{ //
        Word water = collins.queryWord("water");
        Formatter fwater = new Formatter(water);
        fwater.printText();

        Word duplicate = collins.queryWord("duplicate");
        Formatter fduplicate = new Formatter(duplicate);
        fduplicate.printText();

        Word polymorphism = collins.queryWord("polymorphism");
        Formatter fpolymorphism = new Formatter(polymorphism);
        fpolymorphism.printText();

        Word casual = collins.queryWord("casual");
        Formatter fcasual = new Formatter(casual);
        fcasual.printText();

        Word hibernate = collins.queryWord("hibernate");
        Formatter fhibernate = new Formatter(hibernate);
        fhibernate.printText();

        Word hypothesis = collins.queryWord("hypothesis");
        Formatter fhypothesis = new Formatter(hypothesis);
        fhypothesis.printText();

        Word test = collins.queryWord("test");
        Formatter ftest = new Formatter(test);
        ftest.printText();

        Word thes = collins.queryWord("thes");
        Formatter fthes = new Formatter(thes);
        fthes.printText();

        Word ace = collins.queryWord("ace");
        Formatter face = new Formatter(ace);
        face.printText();
        // }}} words //
    }
    // }}} CollinsQuery //

    // Word class serialize and deserialize {{{ //
    // TODO: fix the Word Object serialize method <22-09-20, gk07> //
    @Ignore
    @Test
    public void testWordSerialize() throws IOException {
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word word = collins.queryWord("water");
        String filename = "./src/test/resources/water.ser";
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // serialize
            out.writeObject(word);

            out.close();
            file.close();

            System.out.println("Word Object has been serialized");

        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Caught IOException while serialize");
        }

        Word aWord = null;

        try {
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            aWord = (Word) in.readObject();

            in.close();
            file.close();

            System.out.println("Word Object has been deserialized");
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Caught IOException while deserialize");
        }

        assertEquals("words Should equals", word, aWord);
        Formatter f = new Formatter(aWord);
        Utils.test("Deserialized Word Object: ");
        f.printText();
    }

    // }}} Word class serialize //

    // word clone {{{ //
    @Ignore
    @Test
    public void testWordClone()
        throws CloneNotSupportedException, SQLException{
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word word = defDict.queryWord("ace");

        Word wordCloned = word.clone();

        assertEquals("Cloned word should be equal", word, wordCloned);

        wordCloned.setPronounce(new Pronounce("test for update", "test url"));
        assertEquals("test for update", wordCloned.getPronounce().getSoundmark());
        assertEquals("test url", wordCloned.getPronounce().getSound());
        assertNotEquals("After set, cloned should not equal",
                word, wordCloned);

    }

    // }}} word clone //

    // dict database {{{ //
    @Ignore
    @Test
    public void testDatabaseSetProperties() throws IOException {
        Database db = new Database();
        assertNotNull("db should be a instance of class Database", db);
    }

    @Ignore
    @Test
    public void testDatabaseGetConnection() throws IOException, SQLException {
        Database db = new Database();
        assertNotNull("con should be a instance of class Connection",
                db.getCurrentCon());
        db.closeConnection();
        assertTrue(db.getCurrentCon().isClosed());
    }

    @Ignore
    @Test
    public void testDictHasTables() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        // assertTrue("Should have tables", mitDict.hasTables());
        assertFalse("Should NOT have tables", mitDict.hasTables());
    }

    public void testDictInitializeTables() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        // assertTrue("Should have tables", mitDict.hasTables());
        // assertFalse("Should NOT have tables", mitDict.hasTables());
        mitDict.initializeTables();
        assertTrue("Should have tables", mitDict.hasTables());
    }

    @Ignore
    @Test
    public void testDictDropTables() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        // mitDict.initializeTables();
        mitDict.dropTables();
        assertFalse("Should NOT have tables", mitDict.hasTables());
    }
    // }}} dict database //

    // DefaultLocalDict operate {{{ //
    // Add {{{ //
    @Ignore
    @Test
    public void testDefaultLocalDictAdd() throws IOException, SQLException {
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word water = collins.queryWord("water");
        DefaultLocalDict defDict = new DefaultLocalDict();
        defDict.addWord(water);
        Word hibernate = collins.queryWord("hibernate");
        defDict.addWord(hibernate);
        Word duplicate = collins.queryWord("duplicate");
        defDict.addWord(duplicate);
        Word thes = collins.queryWord("thes");
        defDict.addWord(thes);
        Word casual = collins.queryWord("casual");
        defDict.addWord(casual);
        Word ace = collins.queryWord("ace");
        defDict.addWord(ace);
    }

    // }}} Add //
    // Query {{{ //
    @Ignore
    @Test
    public void testDefaultLocalDictQuery() throws IOException, SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();

        String word = "water";
        System.out.println("### Local ###");
        Word wordLocal = defDict.queryWord(word);
        Formatter fmtLocal = new Formatter(wordLocal);
        fmtLocal.printText();
        System.out.println("### Online ###");
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word wordCollins = collins.queryWord(word);
        Formatter fmtCollins = new Formatter(wordCollins);
        fmtCollins.printText();
        assertEquals("results local and collins of query should equal.", wordCollins, wordLocal);
        assertEquals(wordLocal.getSpell(), wordCollins.getSpell());
        assertEquals(wordLocal.getForms(), wordCollins.getForms());
        assertEquals(wordLocal.getFrequency(), wordCollins.getFrequency());
        assertEquals(wordLocal.getPronounce(), wordCollins.getPronounce());
        assertEquals(wordLocal.getSenesEntries(), wordCollins.getSenesEntries());
        assertEquals(wordLocal.getSource(), wordCollins.getSource());
    }
    // }}} Query //
    // delete {{{ //
    @Ignore
    @Test
    public void testDefaultLocalDictDelete() throws IOException, SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word water = collins.queryWord("water");
        defDict.addWord(water);
        Word queryWater = defDict.queryWord("water");
        assertFalse("word Should not be empty", queryWater.isEmypty());
        defDict.deleteWord("water");
        queryWater = defDict.queryWord("water");
        assertTrue("word Should not be deleted, so empty", queryWater.isEmypty());

        // String word = "ace";
    }
    // }}} delete //
    // update whole word {{{ //
    @Ignore
    @Test
    public void testDefaultLocalUpdate() throws CloneNotSupportedException,
           SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word word1 = collins.queryWord("ace");
        Word word2 = collins.queryWord("ace");


        word1.setPronounce(new Pronounce("test for update", "test url"));
        defDict.updateWord(word1);
        assertEquals("test for update", word1.getPronounce().getSoundmark());
        assertEquals("test url", word1.getPronounce().getSound());

        defDict.updateWord(word2);
    }
    // }}} update whole word //
    // update word access {{{ //
    @Ignore
    @Test
    public void testDefaultLocalUpdateWordAccess() throws SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word table = defDict.queryWord("table");
        int acounter0 = table.getAcounter();
        table = defDict.queryWord("table");
        int acounter1 = table.getAcounter();

        assertEquals("acounter should plus 1", acounter0 + 1, acounter1);
    }
    // }}} update word access //

    // update word mofity {{{ //
    @Ignore
    @Test
    public void testDefaultLocalUpdateWordModify() throws SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word word = defDict.queryWord("ace");
        Instant mtime0 = word.getMtime();
        defDict.updateWordModify(word);
        word = defDict.queryWord("ace");
        Instant mtime1 = word.getMtime();

        assertTrue("mtime should updated", mtime0.compareTo(mtime1) < 0);
    }
    // }}} update word mofity //


    // update word forms {{{ //
    @Ignore
    @Test
    public void testDefaultLocalUpdateWordForms() throws SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word word = defDict.queryWord("ace");
        ArrayList<String> forms0 = word.getForms();
        ArrayList<String> clonedForms = Utils.cloneArrayListString(forms0);
        clonedForms.set(0, "test form");

        defDict.updateWordForms(word, clonedForms);
        word = defDict.queryWord("ace");
        ArrayList<String> forms1 = word.getForms();

        assertEquals("forms should be updated", forms1.get(0), "test form");
        ArrayList<String> a = new ArrayList<>(Arrays.asList("aces"));
        defDict.updateWordForms(word, a);
        word = defDict.queryWord("ace");
        assertEquals("forms should be recovered",
                word.getForms().get(0), "aces");
    }
    // }}} update word forms //

    // update word pronounce {{{ //
    @Ignore
    @Test
    public void testDefaultLocalUpdateWordPronounce()
        throws SQLException,CloneNotSupportedException  {
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word word = defDict.queryWord("ace");
        Pronounce pronounce0 = word.getPronounce();
        String soundmark0 = pronounce0.getSoundmark();
        String sound0 = pronounce0.getSound();
        Pronounce clonedPron = pronounce0.clone();
        clonedPron.setSound("test sound");
        clonedPron.setSoundmark("test soundmark");

        defDict.updateWordPronounce(word, clonedPron);
        word = defDict.queryWord("ace");
        Pronounce pronounce1 = word.getPronounce();

        assertEquals("pronounce should be updated",
                pronounce1.getSoundmark(), "test soundmark");
        defDict.updateWordPronounce(word, pronounce0);
        word = defDict.queryWord("ace");
        assertEquals("pronounce should be recovered",
                word.getPronounce().getSoundmark(), soundmark0);
        assertEquals("pronounce should be recovered",
                word.getPronounce().getSound(), sound0);
    }
    // }}} update word pronounce //

    // update word source {{{ //
    @Test
    public void testDefaultLocalUpdateWordSource()
        throws SQLException,CloneNotSupportedException  {
        DefaultLocalDict defDict = new DefaultLocalDict();
        Word word = defDict.queryWord("ace");
        String source0 = word.getSource();
        String testSource = "test source";

        defDict.updateWordSource(word, testSource);
        word = defDict.queryWord("ace");
        String source1 = word.getSource();

        assertEquals("source should be updated", source1, testSource);
        defDict.updateWordSource(word, source0);
        word = defDict.queryWord("ace");
        assertEquals("source should be recovered", word.getSource(), source0);
    }
    // }}} update word source //
    // size {{{ //
    @Ignore
    @Test
    public void testDefaultLocalSize() throws IOException, SQLException {
        DefaultLocalDict defDict = new DefaultLocalDict();
        defDict.initializeTables();
        int size = defDict.size();
        assertTrue("size of dictionary should be greater or equal than zero",
                size >= 0);

        Utils.test(String.format("{%s} size: %d", defDict.getName(), size));
    }
    // }}} size //
    // }}} MITDict operate //

    // register dicts {{{ //
    // @Ignore
    // @Test
    // public void testRegisterDicts() throws IOException, SQLException {
    //     app.registerDicts();
    // }
    // }}} register dicts //
    // app main {{{ //
    @Ignore
    @Test
    public void testMain() throws IOException, SQLException {
        App.main();
        App.main("duplicate");
        App.main("huddle");
    }
    // }}} app main //
}
