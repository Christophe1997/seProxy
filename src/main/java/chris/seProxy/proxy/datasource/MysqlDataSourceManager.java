package chris.seProxy.proxy.datasource;

import chris.seProxy.util.PropManager;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Optional;

/**
 * MySql DataSource
 */
@Slf4j
public class MysqlDataSourceManager implements DataSourceManager {

    private MysqlDataSource dataSource;

    public MysqlDataSourceManager(PropManager propManager) {
        String url = propManager.getDatabaseUrl();
        String uname = propManager.getDatabaseUsername();
        String password = propManager.getDatabasePassword();
        dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(uname);
        dataSource.setPassword(password);
    }

    @Override
    public DClass getDClass() {
        return DClass.MYSQL;
    }

    @Override
    public MysqlDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Optional<Connection> getConnection() {
        try {
            return Optional.of(dataSource.getConnection());
        } catch (SQLException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void executeUpdate(String sql) {
        log.info("Execute: " + sql);
        getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                int i = stmt.executeUpdate(sql);
                printSQLWarnings(stmt.getWarnings());
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        });
    }



    @Override
    public void printSQLException(SQLException ex) {
        while (ex != null) {
            log.error("SQLState: " + ex.getSQLState());
            log.error("Error Code: " + ex.getErrorCode());
            log.error("Message: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                log.error("Cause: " + t);
                t = t.getCause();
            }
            ex = ex.getNextException();
        }
    }

    @Override
    public void printSQLWarnings(SQLWarning warning) {
        while (warning != null) {
            log.warn("Message: " + warning.getMessage());
            log.warn("SQLState: " + warning.getSQLState());
            log.warn("Vendor error code: " + warning.getErrorCode());
            log.warn("");
            warning = warning.getNextWarning();
        }
    }


}
