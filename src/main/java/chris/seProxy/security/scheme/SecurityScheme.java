package chris.seProxy.security.scheme;

import chris.seProxy.proxy.middleware.Middleware;
import chris.seProxy.rewriter.context.Context;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

/**
 * Core scheme for how to encrypt, which algorithm to use, is it necessary to adjust the db
 */
public interface SecurityScheme {

    String encrypt(Context context, String val);

    /**
     * Middleware that control the db
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

    /**
     * Remove `"` or `'` from a string literal
     * @param s: string may have `'` or `"`
     * @return unwrapped string
     */
    static String unwrapQuote(@NotNull String s) {
        if (s.charAt(0) == '\'' || s.charAt(0) == '"') {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    /**
     * Add `"` to a string, build a string literal
     * @param s: string need wrapQuote
     * @return wrapped string
     */
    @NotNull
    @Contract(pure = true)
    static String wrapQuote(@NotNull String s) {
        return '"' + s + '"';
    }

    @NotNull
    static String encodeAndWrap(byte[] data) {
        return wrapQuote(base64Encode(data));
    }
}
