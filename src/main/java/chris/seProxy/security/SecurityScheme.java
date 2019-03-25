package chris.seProxy.security;

import chris.seProxy.proxy.Middleware;

public interface SecurityScheme {

    String encryptDatabaseName(String name);

    String encryptTableName(String name);

    String encryptViewName(String name);

    String encryptTableColumnName(String tableName, String colName);

    String encryptViewColumnName(String viewName, String colName);

    Middleware middleware();
}
