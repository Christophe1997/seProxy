package chris.seProxy.security;

import chris.seProxy.proxy.BaseMiddleware;
import chris.seProxy.proxy.Middleware;

public class BaseScheme implements SecurityScheme {

    private Middleware middleware;

    public BaseScheme() {
        middleware = new BaseMiddleware();
    }

    @Override
    public String encryptDatabaseName(String name) {
        return name;
    }

    @Override
    public String encryptTableName(String name) {
        return name;
    }

    @Override
    public String encryptViewName(String name) {
        return name;
    }

    @Override
    public String encryptTableColumnName(String tableName, String colName) {
        return colName;
    }

    @Override
    public String encryptViewColumnName(String viewName, String colName) {
        return colName;
    }

    @Override
    public Middleware middleware() {
        return middleware;
    }
}
