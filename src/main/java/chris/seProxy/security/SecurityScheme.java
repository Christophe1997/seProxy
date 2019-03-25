package chris.seProxy.security;

import chris.seProxy.proxy.Middleware;
import chris.seProxy.rewriter.Context;

public interface SecurityScheme {

    String encryptDatabaseName(String name);

    String encryptTableName(String name);

    String encryptViewName(String name);

    String encryptColumnName(Context context, String colName);

    String encrypt(Context context, String val);

    Middleware middleware();
}
