package chris.seProxy.security.scheme;

import chris.seProxy.proxy.datasource.OPEDatasourceManager;
import chris.seProxy.proxy.middleware.Middleware;
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
    public Middleware middleware() {
        return middleware;
    }

    @Override
    public String encrypt(Context context, String val) {
        if (val.toUpperCase().equals("NULL")) return val;

        StringBuilder builder = new StringBuilder();
        context.getCurrentTable().ifPresent(tableName ->
                context.getCurrentCol().ifPresent(colName ->
                        context.getCurrentLevel().ifPresent(minLevel ->
                                middleware.getSpecificLevel(tableName, colName).ifPresent(curLevel -> {
                                    if (minLevel.compareTo(curLevel) <= 0) {
                                        byte[] key = middleware.getSpecificKey(tableName, colName, curLevel);
                                        byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, curLevel)
                                                .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                                        builder.append(dispatchEncrypt(val, curLevel, key, iv));
                                    } else {
                                        middleware.adjustLevel(tableName, colName, minLevel);
                                        byte[] key = middleware.getSpecificKey(tableName, colName, minLevel);
                                        byte[] iv = base64Decode(middleware.getSpecificIv(tableName, colName, minLevel)
                                                .orElseThrow(() -> new RuntimeException("This scheme require a initial vector")));
                                        builder.append(dispatchEncrypt(val, minLevel, key, iv));
                                    }
                                }))));
        return builder.toString();
    }

    @Override
    public String decrypt(String tableName, String colName, String val) {
        StringBuilder builder = new StringBuilder();
        middleware.getSpecificLevel(tableName, colName).ifPresent(level ->
                middleware.getSpecificIv(tableName, colName, level).ifPresent(iv -> {
                    byte[] key = middleware.getSpecificKey(tableName, colName, level);
                    try {
                        switch (level) {
                            case RANDOM:
                                builder.append(new String(randomCipher.decrypt(base64Decode(val),
                                        key, base64Decode(iv))));
                                break;
                            case EQUALITY:
                                builder.append(new String(determineCipher.decrypt(base64Decode(val), key)));
                                break;
                            case ORDER:
                                builder.append(opeCipher.decrypted(new BigInteger(val), key, base64Decode(iv)));
                                break;
                            default:
                                break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                }));
        return builder.toString();
    }

    private String dispatchEncrypt(String val, Level level, byte[] key, byte[] iv) {
        byte[] plaintext = unwrapQuote(val).getBytes(StandardCharsets.UTF_8);
        try {
            switch (level) {
                case RANDOM:
                    return encodeAndWrap(randomCipher.encrypt(plaintext, key, iv));
                case EQUALITY:
                    return encodeAndWrap(determineCipher.encrypt(plaintext, key));
                case ORDER:
                    return opeCipher.encrypt(new BigInteger(val), key, iv).toString();
                default:
                    return val;
            }
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed");
        }
    }
}
