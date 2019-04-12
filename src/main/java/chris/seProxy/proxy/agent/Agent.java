package chris.seProxy.proxy.agent;

import chris.seProxy.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public interface Agent {
    /**
     * Get the database information
     * @return Database object for current database
     */
    Database database();

    void executeUpdate(String sql);

    ResultSet executeQuery(String sql);

    void printSQLException(SQLException ex);

    void printSQLWarnings(SQLWarning warning);
}
