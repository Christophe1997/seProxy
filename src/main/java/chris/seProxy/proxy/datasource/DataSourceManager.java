package chris.seProxy.proxy.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
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

    Optional<Connection> getConnection();
}
