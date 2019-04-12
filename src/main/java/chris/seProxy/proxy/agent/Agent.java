package chris.seProxy.proxy.agent;

import chris.seProxy.proxy.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public interface Agent {

    void executeUpdate(String sql);

    ResultSet executeQuery(String sql);

    Database getDatabase();

    void printSQLException(SQLException ex);

    void printSQLWarnings(SQLWarning warning);
}
