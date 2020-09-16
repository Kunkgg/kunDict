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

    public String dbms;
    public String jarFile;
    public String dbName;
    public String userName;
    public String password;
    public String urlString;

    private String driver;
    private String serverName;
    private int portNumber;
    private Properties prop;

    public Database(String propertiesFileName)
            throws FileNotFoundException, IOException,
            InvalidPropertiesFormatException {
        super();
        this.setProperties(propertiesFileName);
    }

    public Connection getConnection() throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);

        Connection conn = null;
        String currentUrlString = null;

        if (this.dbms.equals("mysql")) {
            currentUrlString = String.format("jdbc:%s://%s:%d/",
                            this.dbms,
                            this.serverName,
                            this.portNumber);
            conn = DriverManager.getConnection(currentUrlString,
                    connectionProps);

            this.urlString = currentUrlString + this.dbName;
            conn.setCatalog(this.dbName);
        } else if (this.dbms.equals("derby")) {
            this.urlString = "jdbc:" + this.dbms + ":" + this.dbName;
            this.urlString = String.format("jdbc:%s:%s;create=true",
                                            this.dbms,
                                            this.dbName);

            conn = DriverManager.getConnection(this.urlString,
                                               connectionProps);

        }
        System.out.println("Connected to database");
        return conn;
    }

    public Connection getConnection(String userName, String password)
            throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        Connection conn = null;
        String currentUrlString = null;

        if (this.dbms.equals("mysql")) {
            currentUrlString = String.format("jdbc:%s://%s:%d/",
                            this.dbms,
                            this.serverName,
                            this.portNumber);
            conn = DriverManager.getConnection(currentUrlString,
                    connectionProps);

            this.urlString = currentUrlString + this.dbName;
            conn.setCatalog(this.dbName);
        } else if (this.dbms.equals("derby")) {
            this.urlString = "jdbc:" + this.dbms + ":" + this.dbName;
            this.urlString = String.format("jdbc:%s:%s;create=true",
                                            this.dbms,
                                            this.dbName);

            conn = DriverManager.getConnection(this.urlString,
                                               connectionProps);

        }
        System.out.println("Connected to database");
        return conn;
    }

    public static void createDatabase(Connection connArg, String dbNameArg,
            String dbmsArg) {

        if (dbmsArg.equals("mysql")) {
            try {
                Statement s = connArg.createStatement();
                String newDatabaseString = "CREATE DATABASE IF NOT EXISTS "
                        + dbNameArg;

                System.out.println("Created database " + dbNameArg);
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
    }

    public static void closeConnection(Connection connArg) {
        System.out.println("Releasing all open resources ...");
        try {
            if (connArg != null) {
                connArg.close();
                connArg = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

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

    private void setProperties(String fileName) throws FileNotFoundException,
            IOException, InvalidPropertiesFormatException {
        this.prop = new Properties();
        FileInputStream fis = new FileInputStream(fileName);
        prop.loadFromXML(fis);

        this.dbms = this.prop.getProperty("dbms");
        this.jarFile = this.prop.getProperty("jar_file");
        this.driver = this.prop.getProperty("driver");
        this.dbName = this.prop.getProperty("database_name");
        this.userName = this.prop.getProperty("user_name");
        this.password = this.prop.getProperty("password");
        this.serverName = this.prop.getProperty("server_name");
        this.portNumber = Integer
                .parseInt(this.prop.getProperty("port_number"));

        System.out.println("Set the following properties:");
        System.out.println("dbms: " + dbms);
        System.out.println("driver: " + driver);
        System.out.println("dbName: " + dbName);
        System.out.println("userName: " + userName);
        System.out.println("serverName: " + serverName);
        System.out.println("portNumber: " + portNumber);
    }
}
