package chris.seProxy;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

/**
 * Database Driver manager
 */
public class Driver {
    private static final String MYSQL_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String ORACLE_JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String SQLITE_JDBC_DRIVER = "org.sqlite.JDBC";

    /**
     * A map from database to driver
     */
    private final HashMap<String, String> driverTable = new HashMap<String, String>() {{
        put("mysql", MYSQL_JDBC_DRIVER);
        put("oracle", ORACLE_JDBC_DRIVER);
        put("postgresql", POSTGRESQL_JDBC_DRIVER);
        put("sqlite", SQLITE_JDBC_DRIVER);
    }};
    /**
     * Default properties file
     */
    private static final String CONFIG_FILE = "connection.prop";

    @Getter
    private String dclass;
    @Getter
    private String url;
    @Getter
    private String uname;
    @Getter
    private String password;

    /**
     * init driver config from the {@link chris.seProxy.Driver#CONFIG_FILE}
     */
    public Driver() {

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream propFile = loader.getResourceAsStream(CONFIG_FILE);
            Properties prop = new Properties();
            prop.load(propFile);
            dclass = (String) prop.get("DBCLASS");
            url = (String) prop.get("DBURL");
            uname = (String) prop.get("UNAME");
            password = (String) prop.get("PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Register dbDriver for dbName
     * @param dbName database name, such as mysql
     * @param dbDriver database driver for dbName, such as com.mysql.jdbc.Driver
     */
    public void register(String dbName, String dbDriver) {
        driverTable.put(dbName, dbDriver);
    }

    /**
     * Connect the database with connection.prop
     * @return Some Connection or None
     */
    public Optional<Connection> getConnection() {
        return Optional.ofNullable(driverTable.get(dclass)).map(driver -> {
            try {
                Class.forName(driver);
                return DriverManager.getConnection(url, uname, password);
            } catch (Exception ex) {
                return null;
            }
        });
    }
}
