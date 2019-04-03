package chris.seProxy.security.scheme;

import chris.seProxy.proxy.Middleware;
import chris.seProxy.rewriter.context.Context;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

/**
 * Core scheme for how to encrypt, which algorithm to use, is it necessary to adjust the database
 */
public interface SecurityScheme {

    String encrypt(Context context, String val);

    /**
     * Middleware that control the database
     * @return {@link Middleware}
     */
    Middleware middleware();

    @NotNull
    @Contract("_ -> new")
    static String base64Encode (byte[] data) {
        return new String(Base64.getEncoder().encode(data));
    }

    static byte[] base64Decode (@NotNull String data) {
        return Base64.getDecoder().decode(data.getBytes());
    }
}
