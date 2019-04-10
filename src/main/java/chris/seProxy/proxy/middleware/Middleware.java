package chris.seProxy.proxy.middleware;

import chris.seProxy.security.Level;

import java.util.List;
import java.util.Optional;

/**
 * Control the db, gather all the information that need for scheme
 */
public interface Middleware {

    /**
     * Get all the columns name from table
     * @param tableName table name
     * @return columns name list
     */
    Optional<List<String>> getColsFromTable(String tableName);

    Optional<Level> getSpecificLevel(String tableName, String colName);

    Optional<String> getSpecificIv(String tableName, String colName, Level level);

    byte[] getSpecificKey(String tableName, String colName, Level level);

    void adjustLevel(String tableName, String colName, Level newLevel);
}
