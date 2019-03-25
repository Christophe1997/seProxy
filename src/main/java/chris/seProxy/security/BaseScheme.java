package chris.seProxy.security;

import chris.seProxy.proxy.BaseMiddleware;
import chris.seProxy.proxy.Middleware;
import chris.seProxy.rewriter.Context;

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
    public String encryptColumnName(Context context, String colName) {
        return colName;
    }

    @Override
    public String encrypt(Context context, String val) {
        return val;
    }


    @Override
    public Middleware middleware() {
        return middleware;
    }
}
