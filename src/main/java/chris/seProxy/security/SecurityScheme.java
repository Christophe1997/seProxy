package chris.seProxy.security;

public interface SecurityScheme {

    String encryptDatabaseName(String name);

    String encryptTableName(String name);

    String encryptColumnName(String tableName, String colName);
}
