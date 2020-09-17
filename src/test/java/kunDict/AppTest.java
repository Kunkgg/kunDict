/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package kunDict;

import org.junit.Test;
import static org.junit.Assert.*;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

public class AppTest {
    @Test
    public void testAppHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull("app should have a greeting",
                classUnderTest.getGreeting());
    }

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

    @Test
    public void testCollinsExtracotr() throws IOException {
        String fileName = "water.html";
        String html = Files.readString(Path.of(fileName));
        Extractor extractor = new Extractor(html);
        Word water = extractor.collinsOnline();
        // System.out.println(water);
        // System.out.println(water.getSpell());
        // System.out.println(water.getPronounce());
        // System.out.println(water.getFrequency());
        assertNotNull("water should be a Word class type", water);
        assertEquals("should be water", "water", water.getSpell());
        assertEquals("frequency should be 5", "frequency band: 5",
                water.getFrequency().toString());
        assertEquals("sensEntryList size should be 5", 5,
                water.getSenesEntry().size());
        assertNotNull("sensEntryList should not Null", water.getSenesEntry());
    }

    @Test
    public void testCollinsQuery() throws IOException {
        String word = "water";
        CollinsOnlineDict collins = new CollinsOnlineDict();
        assertEquals("Should get name of dictionary",
                "Collins Online English Dictionary", collins.getName());
        Word water = collins.query(word);
        // System.out.println(water);
        System.out.println(water.getSpell());
        System.out.println(water.getforms());
        System.out.println(water.getSource());
        System.out.println(water.getPronounce().getSoundmark());
        System.out.println(water.getPronounce().getSound());
        System.out.println(water.getFrequency().getBand());
        System.out.println(water.getFrequency().getDescription());
        for (SenseEntry entry : water.getSenesEntry()) {
            System.out.println(entry);
        }
        assertNotNull("water should be a Word class type", water);
        assertEquals("should be water", "water", water.getSpell());
        assertEquals("frequency should be 5", "frequency band: 5",
                water.getFrequency().toString());
        assertEquals("sensEntryList size should be 5", 5,
                water.getSenesEntry().size());
        assertNotNull("sensEntryList should not Null", water.getSenesEntry());
    }

    // @Test public void testDatabaseSetProperties() throws IOException {
    // String fileName = "./src/main/resources/database.config";
    // Database db = new Database(fileName);
    // assertNotNull("db should be a instance of class Database", db);
    // }

    // @Test
    // public void testDatabaseGetConnection() throws IOException, SQLException {
    //     String dbName = "mit_10k_dict";
    //     Database db = new Database(dbName);
    //     Connection con = db.getConnection();
    //     assertNotNull("con should be a instance of class Connection", con);
    //     Database.closeConnection(con);
    //     assertTrue(con.isClosed());
    // }

    @Test
    public void testMITDictQuery() throws IOException, SQLException {
        MITDict mitDict = new MITDict();

        Word water = mitDict.query("water");
        System.out.println(water);
        assertNotNull("water should be a instance of class Word", water);
    }
}
