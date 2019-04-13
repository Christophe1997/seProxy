package chris.seProxy.proxy.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Optional;

/**
 * DataSource manager
 */
public interface DataSourceManager {

    /**
     * Get the db class
     */
    DClass getDClass();

    DataSource getDataSource();

    void executeUpdate(String sql);

    Optional<Connection> getConnection();

    void printSQLException(SQLException ex);

    void printSQLWarnings(SQLWarning warning);
}
