package chris.seProxy.proxy;

import java.util.List;

/**
 * Control the database, gather all the information that need for scheme
 */
public interface Middleware {

    List<String> getColsFromTable(String tableName);
}
