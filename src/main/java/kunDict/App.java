/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package kunDict;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.InvalidPropertiesFormatException;

public class App {
    public static Database db;
    public static Properties configs;
    private String configFileName = "/home/gk07/Repos/kunDict/src/main/resources/default.config";
    private ArrayList<LocalDict> registeredLocalDicts = new ArrayList<>();
    private ArrayList<OnlineDict> registeredOnlineDicts = new ArrayList<>();

    public App() throws IOException, SQLException {
        this.loadConfigs();
        App.db = new Database();
        this.registerDicts();
    }

    private void loadConfigs() throws FileNotFoundException,
            IOException, InvalidPropertiesFormatException {
        App.configs = new Properties();
        FileInputStream fis = new FileInputStream(this.configFileName);
        App.configs.load(fis);
        fis.close();

        String testMode = App.configs.getProperty("testMode");
        String configMsg = App.configs.getProperty("configMsg");
        String infoMsg = App.configs.getProperty("infoMsg");
        String warningMsg = App.configs.getProperty("warningMsg");
        String debugMsg = App.configs.getProperty("debugMsg");
        String dbms = App.configs.getProperty("dbms");
        String driver = App.configs.getProperty("driver");
        String dbName = App.configs.getProperty("dbName");
        String userName = App.configs.getProperty("userName");
        String password = App.configs.getProperty("password");
        String serverName = App.configs.getProperty("serverName");
        String portNumber = App.configs.getProperty("portNumber");

        Utils.config("Loaded the following configs:");
        Utils.config("config file: " + this.configFileName);
        Utils.config("testMode: " + testMode);
        Utils.config("configMsg: " + configMsg);
        Utils.config("infoMsg: " + infoMsg);
        Utils.config("warningMsg: " + warningMsg);
        Utils.config("debugMsg: " + debugMsg);
        Utils.config("dbms: " + dbms);
        Utils.config("driver: " + driver);
        Utils.config("dbName: " + dbName);
        Utils.config("userName: " + userName);
        Utils.config("password: " + password);
        Utils.config("serverName: " + serverName);
        Utils.config("portNumber: " + portNumber);
    }

    // getter and setter {{{ //
    public String getConfigFileName() {
        return this.configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public ArrayList<Dict> getRegisteredDicts() {
        ArrayList<Dict> localDictsClone =
            (ArrayList<Dict>) this.registeredLocalDicts.clone();
        ArrayList<Dict> onlineDictsClone =
            (ArrayList<Dict>) this.registeredOnlineDicts.clone();

        localDictsClone.addAll(onlineDictsClone);

        return localDictsClone;
    }

    public ArrayList<LocalDict> getRegisteredLocalDicts(){
        return this.registeredLocalDicts;
    }

    public ArrayList<OnlineDict> getRegisteredOnlineDicts() {
        return this.registeredOnlineDicts;
    }

    // }}} getter and setter //

    // initialize app tables {{{ //
    public void initializeTables() throws IOException, SQLException {
        db.getConnection();
        db.createDatabase();
        db.useDbName();

        if (!hasTables()) {
            db.createTable(SQLStr.createTableDicts());
            db.createTable(SQLStr.createTableDictTypes());
            db.addForeignKey(SQLStr.addForeignKeyDictTypeId());
            this.insertValuesIntoDictTypes();
        }

        Utils.info("APP INITED");
    }

    public boolean hasTables() throws IOException, SQLException {
        Boolean result = false;
        Connection con = db.getCurrentConUseDbName();

        try (Statement stmt = con.createStatement();) {
            String query = SQLStr.hasTables("dict");

            // process the ResultSet {{{ //
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<String> existedTables = new ArrayList<>();
            ArrayList<String> designedTables = new ArrayList<>(
                    Arrays.asList(SQLStr.tableListApp));
            while (rs.next()) {
                existedTables.add(rs.getString(1));
            }

            result = existedTables.containsAll(designedTables);

        } catch (SQLException e) {
            Database.printSQLException(e);
        }
        if (result) {
            Utils.info("App tables existed");
        } else {
            Utils.info("App tables NOT existed");
        }

        return result;
        // }}} process the ResultSet //
    }

    public void insertValuesIntoDictTypes() throws SQLException {
        Connection con = db.getCurrentConUseDbName();
        try (Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            for (DictType e : DictType.values()) {
                stmt.addBatch(SQLStr.insertValueIntoDictTypes(e.toString()));
            }
            stmt.executeBatch();
            con.commit();
            Utils.info("Inerted values of dict_types table");
        } catch (BatchUpdateException e) {
            Database.printSQLException(e);
        } catch (SQLException e) {
            Database.printSQLException(e);
        } finally {
            con.setAutoCommit(true);
        }
    }
    // }}} initialize app tables //

    // TODO: check app database status <21-09-20, gk07> //
    // public boolean checkForeignKey(){}
    // public boolean checkTableValuse(){}

    public void registerDicts() throws IOException, SQLException {
        DefaultLocalDict defaultDict = new DefaultLocalDict();
        CollinsOnlineDict collinsDict = new CollinsOnlineDict();

        this.clearRegisteredDicts();

        this.registerDict(defaultDict);
        this.registerDict(collinsDict);
    }

    // register dict {{{ //
    public void registerDict(Dict dict) throws SQLException {
        LocalDict localDict = null;
        String name = dict.getName();
        String shortName = dict.getShortName();
        DictType type = dict.getType();
        int size = (type.equals(DictType.Local)) ? 0 : -1;
        Connection con = db.getCurrentConUseDbName();
        PreparedStatement pstmtDicts = null;
        Statement stmt = null;
        ResultSet rs = null;
        int affectedRow = 0;
        int dictTypeId = 0;

        if (type.equals(DictType.Local)) {
            localDict = (LocalDict) dict;
            size = localDict.size();
            if(!this.registeredLocalDicts.contains(localDict)){
                this.registeredLocalDicts.add(localDict);
            }
        } else {
            OnlineDict onlineDict = (OnlineDict) dict;
            if(!this.registeredOnlineDicts.contains(onlineDict)){
                this.registeredOnlineDicts.add(onlineDict);
            }
        }

        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            pstmtDicts = con.prepareStatement(SQLStr.insertValueIntoDicts(),
                    Statement.RETURN_GENERATED_KEYS);
            rs = stmt.executeQuery(SQLStr.queryDictTypeId(type.toString()));

            if (rs != null && !rs.isClosed() && rs.next()) {
                dictTypeId = rs.getInt(1);
                rs.close();
                Utils.debug("Got dict_type_id: " + dictTypeId);
            }

            if (dictTypeId > 0) {
                pstmtDicts.setString(1, name);
                pstmtDicts.setString(2, shortName);
                pstmtDicts.setInt(3, dictTypeId);
                pstmtDicts.setInt(4, size);
            }
            affectedRow = pstmtDicts.executeUpdate();
            rs = pstmtDicts.getGeneratedKeys();
            if (affectedRow == 1) {
                Utils.info("Registered a dictionary {" + name
                        + "} to App database");
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == SQLStr.ERRORCODE_DUPLICATE_ENTRY) {
                Utils.warning("Duplicated dictionary, please try check.");
            }

            Database.printSQLException(e);
        } finally {
            if (rs != null)
                rs.close();
            if (pstmtDicts != null) {
                pstmtDicts.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null)
                con.setAutoCommit(true);
        }
    }

    public void clearRegisteredDicts() throws SQLException {
        Connection con = db.getCurrentConUseDbName();

        try(Statement stmt = con.createStatement();) {
            int affectedRow = stmt.executeUpdate(SQLStr.clearDicts());
            if (affectedRow > 0) {
                Utils.info("Cleared dicts table");
            } else if (affectedRow == 0) {
                Utils.info("Don't need clear, dicts tables is empty");
            } else {
                Utils.warning("Failed to clear dicts table");
            }
        } catch (SQLException e) {
            Utils.warning("Failed to clear dicts table");
            Database.printSQLException(e);
        }

        this.registeredLocalDicts.clear();
        this.registeredOnlineDicts.clear();
    }
    // }}} register dict //

    public static void main(String... args) throws IOException, SQLException {
        App app = new App();
        Word word = null;
        String hitedDict = null;
        LocalDict defaultDict = app.getRegisteredLocalDicts().get(0);
        String wordSpell = null;
        if (args != null && args.length > 0) {
        ArrayList<Dict> registeredDicts = app.getRegisteredDicts();
        Utils.info("There are " + registeredDicts.size() + " registered dictionarys");
        Utils.debug("RegisteredDicts: " + registeredDicts);

        wordSpell = String.join(" ", args);

        for(Dict dict : registeredDicts) {
            Utils.info(String.format("Searching (%s) in dictionary {%s}",
                        wordSpell, dict.getName()));
            word = dict.queryWord(wordSpell);
            if (word != null && !word.isEmypty()) {
                hitedDict = dict.getName();
                break;
            }
        }

        if (word != null && !word.isEmypty()) {
            Utils.info("==> Get result from " + hitedDict);
            Formatter f = new Formatter(word);
            f.printText();
            if (!hitedDict.equals(defaultDict.getName())) {
                defaultDict.addWord(word);
            }
        } else {
            Utils.warning("Can't find anything");
        }
    } else {
            Utils.warning("Nothing is inputed");
    }
}
}
