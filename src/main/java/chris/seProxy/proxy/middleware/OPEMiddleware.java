package chris.seProxy.proxy.middleware;

import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.agent.OPEAgent;
import chris.seProxy.security.Level;
import chris.seProxy.security.KeyStoreWrapper;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static chris.seProxy.security.scheme.SecurityScheme.*;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<String>> getColsFromTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Level> getSpecificLevel(String tableName, String colName) {
        return Utils.optionalGet(levels.get(tableName), colName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getSpecificIv(String tableName, String colName, Level level) {
        return Utils.optionalGet(ivs.get(tableName), colName);
    }

    private String toKeyAlias(String tableName, String colName, Level level) {
        return tableName + "$" + colName + "$" + level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getSpecificKey(String tableName, String colName, Level level) {
        return keyStoreWrapper.get(toKeyAlias(tableName, colName, level)).map(entry ->
            ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded()
        ).orElseThrow(() -> new RuntimeException("key not exist"));
    }

    /**
     * Adjust strategy for {@link chris.seProxy.security.scheme.OPEScheme}, it only
     * handle RANDOM -> EQUALITY, RANDOM -> ORDER, EQUALITY -> ORDER
     * @param tableName ajust table
     * @param colName adjust column
     * @param newLevel new level
     */
    @Override
    public void adjustLevel(String tableName, String colName, Level newLevel) {
        getSpecificLevel(tableName, colName).ifPresent(curLevel ->
                getSpecificIv(tableName, colName, curLevel).ifPresent(iv -> {
                    byte[] key = getSpecificKey(tableName, colName, curLevel);
                    switch (curLevel) {
                        case RANDOM:
                            adjustRandomTo(newLevel, key, base64Decode(iv), tableName, colName);
                            break;
                        case EQUALITY:
                            adjustEqualityTo(newLevel, key, base64Decode(iv), tableName, colName);
                            break;
                        default:
                            break;
                    }
                }));
    }

    /**
     * Handle RANDOM -> EQUALITY, RANDOM -> ORDER
     */
    private void adjustRandomTo(Level newLevel, byte[] key, byte[] iv, String tableName, String colName) {
        ResultSet rs = Utils.selectCol(agent, tableName, colName);
        try {
            while (rs.next()) {
                String id = rs.getString(0);
                String val = rs.getString(1);
                byte[] decryptedVal = randomCipher.decrypt(base64Decode(val), key, iv);
                switch (newLevel) {
                    case EQUALITY:
                        byte[] newKey1 = determineCipher.generateKey();
                        keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                                (SecretKey) determineCipher.toKey(newKey1));
                        String newVal1 = base64Encode(determineCipher.encrypt(decryptedVal, newKey1, iv));
                        Utils.updateCol(agent, tableName, colName, id, newVal1);
                        break;
                    case ORDER:
                        byte[] newKey2 = opeCipher.generateKey();
                        keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                                (SecretKey) opeCipher.toKey(newKey2));
                        BigInteger newVal2 = opeCipher.encrypt(new BigInteger(new String(decryptedVal)), key, iv);
                        Utils.updateCol(agent, tableName, colName, id, newVal2.toString());
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            agent.printSQLException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle EQUALITY -> ORDER
     */
    private void adjustEqualityTo(Level newLevel, byte[] key, byte[] iv, String tableName, String colName) {
        ResultSet rs = Utils.selectCol(agent, tableName, colName);
        try {
            while (rs.next()) {
                String id = rs.getString(0);
                String val = rs.getString(1);
                byte[] decryptedVal = determineCipher.decrypt(base64Decode(val), key, iv);
                if (newLevel == Level.ORDER) {
                    byte[] newKey = opeCipher.generateKey();
                    keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                            (SecretKey) opeCipher.toKey(newKey));
                    BigInteger newVal = opeCipher.encrypt(new BigInteger(new String(decryptedVal)), key, iv);
                    Utils.updateCol(agent, tableName, colName, id, newVal.toString());
                }
            }
        } catch (SQLException ex) {
            agent.printSQLException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
