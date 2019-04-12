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
     *
     * @param tableName table name
     * @return columns name list
     */
    Optional<List<String>> getColsFromTable(String tableName);

    /**
     * Get the level of specific column
     *
     * @param tableName table name
     * @param colName   column name
     * @return column's level
     */
    Optional<Level> getSpecificLevel(String tableName, String colName);

    /**
     * Get the initial vector of specific column
     *
     * @param tableName table name
     * @param colName   column name
     * @param level     column level
     * @return intial vector
     */
    Optional<String> getSpecificIv(String tableName, String colName, Level level);

    /**
     * Get the key of specific column used in encryption, it should handle the exception if key not exist.
     *
     * @param tableName table name
     * @param colName   column name
     * @param level     column level
     * @return encryption key used with specific condition
     */
    byte[] getSpecificKey(String tableName, String colName, Level level);

    /**
     * Adjust column level to newlevel
     *
     * @param tableName table name
     * @param colName   column name
     * @param newLevel  new level
     */
    void adjustLevel(String tableName, String colName, Level newLevel);

    /**
     * Init database with specific scheme
     */
    void initDatabase();
}
