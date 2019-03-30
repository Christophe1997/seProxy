package chris.seProxy.security.scheme;

import chris.seProxy.proxy.Middleware;
import chris.seProxy.rewriter.context.Context;

public interface SecurityScheme {

    String encrypt(Context context, String val);

    Middleware middleware();


}
