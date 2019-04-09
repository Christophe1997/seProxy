package chris.seProxyTest;

import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.proxy.datasource.MysqlDataSourceManager;
import chris.seProxy.util.PropManager;
import org.junit.Test;

import java.sql.SQLException;

public class MysqlProxyTest {

    private static final DataSourceManager dataSourceManager = new MysqlDataSourceManager(new PropManager());

    @Test
    public void AgentTestShouldPass() {
        dataSourceManager.getConnection().ifPresent(conn -> {

        });
    }
}
