package chris.seProxyTest;

import chris.seProxy.exception.ConnectionFailure;
import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.proxy.datasource.MysqlDataSourceManager;
import chris.seProxy.util.PropManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DriverTest {

    public static void main(String[] args) throws Exception {
        DataSourceManager dataSourceManager = new MysqlDataSourceManager(new PropManager());
        Connection coon = dataSourceManager.getConnection().orElseThrow(() -> new ConnectionFailure("connection failed"));
        Statement stmt = coon.createStatement();
        ResultSet rs = stmt.executeQuery("select * from student limit 10");
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }
}
