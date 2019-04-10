package chris.seProxy.proxy.middleware;

import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.agent.OPEAgent;
import chris.seProxy.security.Level;
import chris.seProxy.security.KeyStoreWrapper;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class OPEMiddleware implements Middleware {
    private HashMap<String, HashMap<String, Level>> levels;
    private HashMap<String, List<String>> tables;
    private HashMap<String, HashMap<String, String>> ivs;
    private KeyStoreWrapper keyStoreWrapper;
    private OPEAgent agent;
    private IvCipher randomCipher;
    private IvCipher determineCipher;
    private OPECipher opeCipher;

    public OPEMiddleware(OPEAgent agent, KeyStoreWrapper wrapper,
                         IvCipher randomCipher, IvCipher determineCipher, OPECipher opeCipher) {
        this.agent = agent;
        keyStoreWrapper = wrapper;
        this.randomCipher = randomCipher;
        this.determineCipher = determineCipher;
        this.opeCipher = opeCipher;
        levels = agent.initLevelsInfo();
        tables = agent.initTablesInfo();
        ivs = agent.initIvsInfo();
    }

    @Override
    public Optional<List<String>> getColsFromTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    @Override
    public Optional<Level> getSpecificLevel(String tableName, String colName) {
        return Utils.optionalGet(levels.get(tableName), colName);
    }

    @Override
    public Optional<String> getSpecificIv(String tableName, String colName, Level level) {
        return Utils.optionalGet(ivs.get(tableName), colName);
    }

    private String toKeyAlias(String tableName, String colName, Level level) {
        return tableName + "$" + colName + "$" + level;
    }

    @Override
    public byte[] getSpecificKey(String tableName, String colName, Level level) {
        return keyStoreWrapper.get(toKeyAlias(tableName, colName, level)).map(entry ->
            ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded()
        ).orElseThrow(() -> new RuntimeException("key not exist"));
    }

    @Override
    public void adjustLevel(String tableName, String colName, Level newLevel) {
        getSpecificLevel(tableName, colName).ifPresent(curLevel ->
                getSpecificIv(tableName, colName, curLevel).ifPresent(iv -> {
                    byte[] key = getSpecificKey(tableName, colName, curLevel);
                    // TODO
                }));
    }


}
