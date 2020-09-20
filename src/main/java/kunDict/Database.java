package kunDict;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.*;
import java.io.*;
import java.sql.BatchUpdateException;
import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLWarning;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

public class Database {

    private String dbms;
    private String dbName;
    private String userName;
    private String password;
    private String urlString;
    private String propertiesFileName = "./src/main/resources/database.config";

    private Connection currentCon;

    private String driver;
    private String serverName;
    private int portNumber;
    private Properties prop;

    public Database() throws FileNotFoundException,
            IOException, InvalidPropertiesFormatException {
        super();
        this.setProperties();
    }

    public Database(String dbName) throws FileNotFoundException,
            IOException, InvalidPropertiesFormatException {
        this();
        this.setDbName(dbName);
    }

    // manage connection {{{ //
    /** getConnection
      * @param useDbName wether use the this.dbName nor not
    */
    public void getConnectionUseDb() throws SQLException {
        if (! this.isConnected()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", this.userName);
            connectionProps.put("password", this.password);

            Connection con = null;
            String currentUrlString = null;

            if (this.dbms.equals("mysql")) {
                currentUrlString = String.format("jdbc:%s://%s:%d/", this.dbms,
                        this.serverName, this.portNumber);
                con = DriverManager.getConnection(currentUrlString,
                        connectionProps);
                this.currentCon = con;
                Utils.info("Connected to " + this.dbms);
            }

            this.urlString = currentUrlString + this.dbName;
            con.setCatalog(this.dbName);
            this.currentCon = con;
            Utils.info("Using database " + this.dbName);
        }
    }

    public void getConnection() throws SQLException {
        if (! this.isConnected()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", this.userName);
            connectionProps.put("password", this.password);

            Connection con = null;
            String currentUrlString = null;

            if (this.dbms.equals("mysql")) {
                currentUrlString = String.format("jdbc:%s://%s:%d/", this.dbms,
                        this.serverName, this.portNumber);
                con = DriverManager.getConnection(currentUrlString,
                        connectionProps);
                this.currentCon = con;
                Utils.info("Connected to " + this.dbms);
            }
        }
    }

    public void closeConnection() {
        Utils.info("Releasing all open resources ...");
        try {
            if (this.currentCon != null) {
                this.currentCon.close();
                this.currentCon = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public boolean isConnected() throws SQLException{
        boolean result = false;
        if (this.currentCon != null && this.currentCon.isValid(1)) {
            result = true;
        }
        return result;
    }

    public Connection getCurrentConUseDb() throws SQLException{
        this.getConnectionUseDb();
        return this.currentCon;
    }

    public Connection getCurrentCon() throws SQLException{
        this.getConnection();
        return this.currentCon;
    }
    // }}} manage connection //

    // manage table and database {{{ //
    public void createTable(String createTableStr) {
        if (this.dbms.equals("mysql")) {
            try (Statement stmt = this.currentCon.createStatement()){
                stmt.executeUpdate(createTableStr);
                Utils.info("Created Table " + createTableStr.split(" ")[2]);
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
    }

    public void addForeignKey(String addForeignKeyStr) {
        if (this.dbms.equals("mysql")) {
            try (Statement stmt = this.currentCon.createStatement()){
                stmt.executeUpdate(addForeignKeyStr);
                Utils.info("Added foreignKey "
                        + addForeignKeyStr.split(" ")[5]);
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
    }

    public void dropTable(String dropTableStr) {
        String[] temp = dropTableStr.split(" ");
        if (this.dbms.equals("mysql")) {
            try (Statement stmt = this.currentCon.createStatement()) {
                stmt.executeUpdate(dropTableStr);
                Utils.info("Droped table " + String.join(" ",
                        Arrays.copyOfRange(temp, 2, temp.length)));
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
    }

    public static void createDatabase(Connection con, String dbName,
            String dbms) {

        if (dbms.equals("mysql")) {
            try {
                Statement stmt = con.createStatement();
                String newDatabaseString = "CREATE DATABASE IF NOT EXISTS "
                        + dbName;

                System.out.println("Created database " + dbName);
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
    }
    // }}} Create table and database //

    // static methods {{{ //

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (ignoreSQLException(
                        ((SQLException) e).getSQLState()) == false) {
                    e.printStackTrace(System.err);
                    System.err.println(
                            "SQLState: " + ((SQLException) e).getSQLState());
                    System.err.println("Error Code: "
                            + ((SQLException) e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());
                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
        return false;
    }

    public static void getWarningsFromResultSet(ResultSet rs)
            throws SQLException {
        Database.printWarnings(rs.getWarnings());
    }

    public static void getWarningsFromStatement(Statement stmt)
            throws SQLException {
        Database.printWarnings(stmt.getWarnings());
    }

    public static void printWarnings(SQLWarning warning) throws SQLException {
        if (warning != null) {
            System.out.println("\n---Warning---\n");
            while (warning != null) {
                System.out.println("Message: " + warning.getMessage());
                System.out.println("SQLState: " + warning.getSQLState());
                System.out.print("Vendor error code: ");
                System.out.println(warning.getErrorCode());
                System.out.println("");
                warning = warning.getNextWarning();
            }
        }
    }
    // }}} static methods //

    // load database config from property file {{{ //
    private void setProperties() throws FileNotFoundException,
            IOException, InvalidPropertiesFormatException {
        this.prop = new Properties();
        FileInputStream fis = new FileInputStream(this.propertiesFileName);
        prop.load(fis);
        fis.close();

        this.dbms = this.prop.getProperty("dbms");
        this.driver = this.prop.getProperty("driver");
        this.dbName = this.prop.getProperty("dbName");
        this.userName = this.prop.getProperty("userName");
        this.password = this.prop.getProperty("password");
        this.serverName = this.prop.getProperty("serverName");
        this.portNumber = Integer
                .parseInt(this.prop.getProperty("portNumber"));

        Utils.config("Set the following properties:");
        Utils.config("config file: " + this.propertiesFileName);
        Utils.config("dbms: " + dbms);
        Utils.config("driver: " + driver);
        Utils.config("dbName: " + dbName);
        Utils.config("userName: " + userName);
        Utils.config("serverName: " + serverName);
        Utils.config("portNumber: " + portNumber);
    }
    // }}} load database config from property file //

// getter {{{ //
    public String getDbms() {
        return this.dbms;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUrlString() {
        return this.urlString;
    }

    public String getPropertiesFileName() {
        return this.propertiesFileName;
    }

    public String getDriver() {
        return this.driver;
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getPortNumber() {
        return this.portNumber;
    }
    public Properties getProrP() {
        return this.prop;
    }

// }}} getter //

// setter {{{ //
    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password= password;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public void setPropertiesFileName(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setProp(Properties prop) {
        this.prop = prop;
    }

    public void setCurrentCon(Connection currentCon){
        this.currentCon = currentCon;
    }
// }}} setter //
}
