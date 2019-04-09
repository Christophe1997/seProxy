package chris.seProxy.proxy.datasource;

import chris.seProxy.util.PropManager;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * MySql DataSource
 */
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
}
