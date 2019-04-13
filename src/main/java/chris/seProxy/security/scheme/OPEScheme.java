package chris.seProxy.security.scheme;

import chris.seProxy.proxy.datasource.OPEDatasourceManager;
import chris.seProxy.proxy.middleware.OPEMiddleware;
import chris.seProxy.rewriter.context.Context;
import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.KeyStoreWrapper;
import chris.seProxy.security.Level;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;
import chris.seProxy.security.cipher.ciphers.AESCipher;
import chris.seProxy.security.cipher.ciphers.boldyreva.BoldyrevaCipher;
import chris.seProxy.util.PropManager;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static chris.seProxy.security.scheme.SecurityScheme.*;

/**
 * This scheme only work for order preserving encryption, That means
 * it only support for {@link Level#EQUALITY} and
 * {@link Level#ORDER}
 */
public class OPEScheme extends BaseScheme {
    private OPEMiddleware middleware;

    private IvCipher randomCipher;

    private IvCipher determineCipher;

    private OPECipher opeCipher;

    public OPEScheme(PropManager propManager) {
        randomCipher = new AESCipher(Mode.CBC, Padding.PKCS5);
        determineCipher = new AESCipher(Mode.ECB, Padding.PKCS5);
        opeCipher = new BoldyrevaCipher();

        OPEDatasourceManager manage = new OPEDatasourceManager(propManager);
        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(propManager);
        middleware = new OPEMiddleware(manage, keyStoreWrapper, randomCipher, determineCipher, opeCipher);
        if (!propManager.isInit()) {
            middleware.initDatabase();
        }
        middleware.init();
    }
//    rewrite col name code
//    @Override
//    public String rewriteCol(Context context, String colName) {
//        String tableName = context.getCurrentTable().orElseThrow(
//                () -> new RuntimeException("no table set in context"));
//        Level level = middleware.getSpecificLevel(tableName, colName).orElseThrow(
//                () -> new RuntimeException("no level set for " + tableName + "(" + colName + ")"));
//        return colName + "_" + level;
//    }

    @Override
    public String encrypt(Context context, String val) {
        if (val.toUpperCase().equals("NULL")) return val;
        StringBuilder builder = new StringBuilder();
        context.getCurrentTable().ifPresent(tableName ->
                context.getCurrentCol().ifPresent(colName ->
                        context.getCurrentLevel().ifPresent(minProperty ->
                                middleware.getSpecificLevel(tableName, colName).ifPresent(curProperty -> {
                                    if (minProperty.compareTo(curProperty) < 0) {
                                        byte[] key = middleware.getSpecificKey(tableName, colName, curProperty);
                                        byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, curProperty)
                                                .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                                        builder.append(dispatchEncrypt(val, curProperty, key, iv));
                                    } else {
                                        middleware.adjustLevel(tableName, colName, minProperty);
                                        byte[] key = middleware.getSpecificKey(tableName, colName, minProperty);
                                        byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, minProperty)
                                                .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                                        builder.append(dispatchEncrypt(val, minProperty, key, iv));
                                    }
                                }))));
        return builder.toString();
    }

    private String dispatchEncrypt(String val, Level level, byte[] key, byte[] iv) {
        byte[] plaintext = unwrapQuote(val).getBytes(StandardCharsets.UTF_8);
        try {
            switch (level) {
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
