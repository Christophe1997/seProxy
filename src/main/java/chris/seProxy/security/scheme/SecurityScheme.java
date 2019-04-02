package chris.seProxy.security.scheme;

import chris.seProxy.proxy.Middleware;
import chris.seProxy.rewriter.context.Context;

import java.util.Base64;

public interface SecurityScheme {

    String encrypt(Context context, String val);

    Middleware middleware();

    default String base64Encode (byte[] data) {
        return new String(Base64.getEncoder().encode(data));
    }

}
