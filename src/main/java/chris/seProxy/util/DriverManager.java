package chris.seProxy.util;

import lombok.Getter;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Optional;

/**
 * Database Driver manager as a wrapper of {@link java.sql.DriverManager}
 */
public class DriverManager {
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
    @Getter
    private String dclass;
    @Getter
    private String url;
    @Getter
    private String uname;
    @Getter
    private String password;

    /**
     * init driver config from a {@link PropManager}
     * @param propManager {@link PropManager}
     */
    public DriverManager(PropManager propManager) {
        dclass = propManager.getDatabaseClass();
        url = propManager.getDatabaseUrl();
        uname = propManager.getDatabaseUsername();
        password = propManager.getDatabasePassword();
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
                return java.sql.DriverManager.getConnection(url, uname, password);
            } catch (Exception ex) {
                return null;
            }
        });
    }
}
