package chris.seProxy.security.scheme;

import chris.seProxy.proxy.middleware.Middleware;
import chris.seProxy.rewriter.context.Context;

import java.sql.ResultSetMetaData;


/**
 * Default scheme as identity
 */
public class BaseScheme implements SecurityScheme {

    private Middleware middleware;

    @Override
    public String encrypt(Context context, String val) {
        return val;
    }

    @Override
    public String decrypt(String tableName, String colName, String val) {
        return val;
    }


    @Override
    public Middleware middleware() {
        return middleware;
    }
}
