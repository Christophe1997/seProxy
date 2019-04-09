package chris.seProxy.proxy.agent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public interface Agent {
    void executeUpdate(String sql);

    ResultSet executeQuery(String sql);

    void printSQLException(SQLException ex);

    void printSQLWarnings(SQLWarning warning);
}
