package chris.seProxy.security.scheme;

import chris.seProxy.proxy.middleware.BaseMiddleware;
import chris.seProxy.proxy.middleware.Middleware;
import chris.seProxy.rewriter.context.Context;
import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.Property;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;
import chris.seProxy.security.cipher.ciphers.AESCipher;
import chris.seProxy.security.cipher.ciphers.boldyreva.BoldyrevaCipher;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static chris.seProxy.security.scheme.SecurityScheme.*;

/**
 * This scheme only work for order preserving encryption, That means
 * it only support for {@link chris.seProxy.security.Property#EQUALITY} and
 * {@link chris.seProxy.security.Property#ORDER}
 */
public class OPEScheme extends BaseScheme {
    private Middleware middleware;

    private IvCipher randomCipher;

    private IvCipher determineCipher;

    private OPECipher opeCipher;

    public OPEScheme() throws Exception {
        middleware = new BaseMiddleware();
        randomCipher = new AESCipher(Mode.CBC, Padding.PKCS5);
        determineCipher = new AESCipher(Mode.ECB, Padding.PKCS5);
        opeCipher = new BoldyrevaCipher();
    }

    @Override
    public String encrypt(Context context, String val) {
        if (val.toUpperCase().equals("NULL")) return val;
        StringBuilder builder = new StringBuilder();
        context.getCurrentTable().ifPresent(tableName -> {
            context.getCurrentCol().ifPresent(colName -> {
                context.getCurrentProperty().ifPresent(minProperty -> {
                    middleware.getSpecificLevel(tableName, colName).ifPresent(curProperty -> {
                        if (minProperty.compareTo(curProperty) < 0) {
                            byte[] key = middleware.getSpecificKey(tableName, colName, curProperty);
                            byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, curProperty)
                                    .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                            builder.append(dispatchEncrypt(val, curProperty, key, iv));
                        } else {
                            middleware.adjustProperty(tableName, colName, minProperty);
                            byte[] key = middleware.getSpecificKey(tableName, colName, minProperty);
                            byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, minProperty)
                                    .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                            builder.append(dispatchEncrypt(val, minProperty, key, iv));
                        }
                    });
                });
            });
        });
        return builder.toString();
    }

    private String dispatchEncrypt(String val, Property property, byte[] key, byte[] iv) {
        byte[] plaintext = unwrapQuote(val).getBytes(StandardCharsets.UTF_8);
        try {
            switch (property) {
                case RANDOM:
                    return encodeAndWrap(randomCipher.encrypt(plaintext, key, iv));
                case EQUALITY:
                    return encodeAndWrap(determineCipher.encrypt(plaintext, key, iv));
                case ORDER:
                    return opeCipher.encrypt(new BigInteger(val), key).toString();
                default:
                    return val;
            }
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed");
        }
    }
}
