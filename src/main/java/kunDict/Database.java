package kunDict;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

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
    public void getConnectionUseDbName() throws SQLException {
        this.getConnection();
        this.useDbName();
    }

    public void useDbName() throws SQLException {
        if ( this.isConnected()) {
            this.urlString = this.urlString + this.dbName;
            this.currentCon.setCatalog(this.dbName);
            Utils.info("Using database: " + this.dbName);
        }
    }

    public void getConnection() throws SQLException {
        if (! this.isConnected()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", this.userName);
            connectionProps.put("password", this.password);

            if (this.dbms.equals("mysql")) {
                this.urlString = String.format("jdbc:%s://%s:%d/", this.dbms,
                        this.serverName, this.portNumber);
                this.currentCon = DriverManager.getConnection(this.urlString,
                        connectionProps);
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

    public Connection getCurrentConUseDbName() throws SQLException{
        this.getConnectionUseDbName();
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

    public void createDatabase() {
        if (this.dbms.equals("mysql")) {
            try (Statement stmt = this.currentCon.createStatement()) {
                String createDatabaseStr = SQLStr.createDb(this.dbName);
                stmt.executeUpdate(createDatabaseStr);
                Utils.info("Created database " + this.dbName);
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
                    Utils.err("SQLState: "
                            + ((SQLException) e).getSQLState());
                    Utils.err("Error Code: "
                            + ((SQLException) e).getErrorCode());
                    Utils.err("Message: " + e.getMessage());
                    Throwable t = ex.getCause();
                    while (t != null) {
                        Utils.warning("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            Utils.warning("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase(SQLStr.SQLSTATE_JAR_FILE_EXISTED))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase(SQLStr.SQLSTATE_TABLE_EXISTED))
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
            Utils.warning("\n---Warning---\n");
            while (warning != null) {
                Utils.warning("Message: " + warning.getMessage());
                Utils.warning("SQLState: " + warning.getSQLState());
                Utils.warning("Vendor error code: " + warning.getErrorCode());
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
        if (this.prop.getProperty("test").equals("true")) {
            this.dbName = this.prop.getProperty("dbName") + "_test";
        } else {
            this.dbName = this.prop.getProperty("dbName");
        }
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
