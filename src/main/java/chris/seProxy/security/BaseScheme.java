package chris.seProxy.security;

public class BaseScheme implements SecurityScheme {

    @Override
    public String encryptDatabaseName(String name) {
        return name;
    }

    @Override
    public String encryptTableName(String name) {
        return name;
    }

    @Override
    public String encryptColumnName(String tableName, String colName) {
        return colName;
    }
}
