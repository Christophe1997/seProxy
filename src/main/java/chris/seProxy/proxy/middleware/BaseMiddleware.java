package chris.seProxy.proxy.middleware;

import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.agent.BaseAgent;
import chris.seProxy.security.Property;
import chris.seProxy.security.KeyStoreWrapper;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BaseMiddleware implements Middleware {
    private HashMap<String, HashMap<String, Property>> levels;
    private HashMap<String, List<String>> tables;
    private HashMap<String, HashMap<String, String>> ivs;
    private KeyStoreWrapper keyStoreWrapper;
    private BaseAgent agent;

    public BaseMiddleware(BaseAgent agent, KeyStoreWrapper wrapper) {
        this.agent = agent;
        keyStoreWrapper = wrapper;
        levels = agent.initLevelsInfo();
        tables = agent.initTablesInfo();
        ivs = agent.initIvsInfo();
    }

    @Override
    public Optional<List<String>> getColsFromTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    @Override
    public Optional<Property> getSpecificLevel(String tableName, String colName) {
        return Utils.optionalGet(levels.get(tableName), colName);
    }

    @Override
    public Optional<String> getSpecificIv(String tableName, String colName, Property property) {
        return Utils.optionalGet(ivs.get(tableName), colName);
    }

    private String toKeyAlias(String tableName, String colName, Property property) {
        return tableName + "$" + colName + "$" + property;
    }

    @Override
    public byte[] getSpecificKey(String tableName, String colName, Property property) {
        return keyStoreWrapper.get(toKeyAlias(tableName, colName, property)).map(entry ->
            ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded()
        ).orElseThrow(
                () -> new RuntimeException("key not exist")
        );
    }

    @Override
    public void adjustProperty(String tableName, String colName, Property property) {

    }

}
