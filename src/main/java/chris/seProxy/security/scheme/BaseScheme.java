package chris.seProxy.security.scheme;

import chris.seProxy.proxy.middleware.OPEMiddleware;
import chris.seProxy.proxy.middleware.Middleware;
import chris.seProxy.rewriter.context.Context;


/**
 * Default scheme as identity
 */
public class BaseScheme implements SecurityScheme {

    private Middleware middleware;

    public BaseScheme() {
        middleware = new OPEMiddleware();
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
