package chris.seProxy.proxy.middleware;

import chris.seProxy.security.Property;

import java.util.List;
import java.util.Optional;

/**
 * Control the database, gather all the information that need for scheme
 */
public interface Middleware {

    /**
     * Get all the columns name from table
     * @param tableName table name
     * @return columns name list
     */
    Optional<List<String>> getColsFromTable(String tableName);

    Optional<Property> getSpecificLevel(String tableName, String colName);

    Optional<String> getSpecificIv(String tableName, String colName, Property property);

    byte[] getSpecificKey(String tableName, String colName, Property property);

    void adjustProperty(String tableName, String colName, Property property);
}
