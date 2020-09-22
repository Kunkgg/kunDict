/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package kunDict;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM)
public class AppTest {
    public App app;

    public AppTest() throws IOException, SQLException{
        this.app = new App();
    }

    // App database {{{ //
    @Ignore
    @Test
    public void testAppInitializeTables() throws IOException, SQLException {
        app.initializeTables();
        assertTrue("Should have tables", app.hasTables());
    }

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
    public void testRequest() {
        String url = "https://www.collinsdictionary.com/us/dictionary/english/water";
        String fileName = "water.html";
        Request req = new Request(url);
        req.bodyHandler = HttpResponse.BodyHandlers
                .ofFile(Paths.get(fileName));
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
        String dbName = "dict";
        Database db = new Database(dbName);
        db.getConnection();
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

    @Ignore
    @Test
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

    // MITDict operate {{{ //
    // Add {{{ //
    @Ignore
    @Test
    public void testMITDictAdd() throws IOException, SQLException {
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word water = collins.queryWord("water");
        MITDict mitDict = new MITDict();
        mitDict.addWord(water);
        Word hibernate = collins.queryWord("hibernate");
        mitDict.addWord(hibernate);
        Word duplicate = collins.queryWord("duplicate");
        mitDict.addWord(duplicate);
        Word thes = collins.queryWord("thes");
        mitDict.addWord(thes);
        Word casual = collins.queryWord("casual");
        mitDict.addWord(casual);
        Word ace = collins.queryWord("ace");
        mitDict.addWord(ace);
    }

    // }}} Add //
    // Query {{{ //
    @Ignore
    @Test
    public void testMITDictQuery() throws IOException, SQLException {
        MITDict mitDict = new MITDict();

        String word = "ace";
        System.out.println("### Local ###");
        Word wordLocal = mitDict.queryWord(word);
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
    public void testMITDictDelete() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word water = collins.queryWord("water");
        mitDict.addWord(water);
        Word queryWater = mitDict.queryWord("water");
        assertFalse("word Should not be empty", queryWater.isEmypty());
        mitDict.deleteWord("water");
        queryWater = mitDict.queryWord("water");
        assertTrue("word Should not be deleted, so empty", queryWater.isEmypty());

        mitDict.deleteWord("water");
        mitDict.deleteWord("water");
        // String word = "ace";
    }
    // }}} delete //
    // update {{{ //
    @Ignore
    @Test
    public void testMITDictUpdate() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        CollinsOnlineDict collins = new CollinsOnlineDict();
        Word word = collins.queryWord("ace");
        word.setPronounce(new Pronounce("test for update", "test url"));
        mitDict.updateWord(word);
        word = mitDict.queryWord("ace");
        assertEquals("test for update", word.getPronounce().getSoundmark());
        assertEquals("test url", word.getPronounce().getSound());

        // String word = "ace";
    }
    // }}} update //
    // size {{{ //
    @Test
    public void testMITDictSize() throws IOException, SQLException {
        MITDict mitDict = new MITDict();
        int size = mitDict.size();
        assertTrue("size of dictionary should be greater or equal than zero",
                size >= 0);
        Utils.test(String.format("{%s} size: %d", mitDict.getName(), size));
    }
    // }}} size //
    // }}} MITDict operate //
}
