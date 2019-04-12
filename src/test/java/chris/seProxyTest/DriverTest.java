package chris.seProxyTest;

import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.proxy.datasource.MysqlDataSourceManager;
import chris.seProxy.util.PropManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DriverTest {

    public static void main(String[] args) {
        DataSourceManager dataSourceManager = new MysqlDataSourceManager(new PropManager());
        dataSourceManager.getConnection().ifPresent(conn -> {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from student limit 10");
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}
