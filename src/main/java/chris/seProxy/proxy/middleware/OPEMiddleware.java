package chris.seProxy.proxy.middleware;

import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.datasource.OPEDatasourceManager;
import chris.seProxy.proxy.db.Column;
import chris.seProxy.proxy.db.Database;
import chris.seProxy.proxy.db.Table;
import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.KeyStoreWrapper;
import chris.seProxy.security.Level;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;
import chris.seProxy.security.cipher.ciphers.AESCipher;
import chris.seProxy.security.cipher.ciphers.boldyreva.BoldyrevaCipher;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static chris.seProxy.security.scheme.SecurityScheme.*;

public class OPEMiddleware implements Middleware {
    private HashMap<String, HashMap<String, Level>> levels;
    private HashMap<String, List<String>> tables;
    private HashMap<String, HashMap<String, String>> ivs;
    private KeyStoreWrapper keyStoreWrapper;
    private OPEDatasourceManager manager;
    private IvCipher randomCipher;
    private IvCipher determineCipher;
    private OPECipher opeCipher;

    private HashMap<String, List<String>> configMap;


    // SQL statement
    private static final String SELECT_CONFIG = "SELECT config.tableName, config.columnName, config.iv, config.level FROM config";
    private static final String CREATE_CONFIG = "CREATE TABLE IF NOT EXISTS config (" +
            "tableName VARCHAR(300) NOT NULL, " +
            "columnName VARCHAR(300) NOT NULL, " +
            "iv VARCHAR(300) NOT NULL, " +
            "level VARCHAR(300) NOT NULL) ENGINE=INNODB";

    public OPEMiddleware(OPEDatasourceManager manager, KeyStoreWrapper wrapper,
                         IvCipher randomCipher, IvCipher determineCipher, OPECipher opeCipher) {
        this.manager = manager;
        keyStoreWrapper = wrapper;
        this.randomCipher = randomCipher;
        this.determineCipher = determineCipher;
        this.opeCipher = opeCipher;
    }

    public void init() {
        initConfigTable();
        levels = initLevelsInfo();
        tables = initTablesInfo();
        ivs = initIvsInfo();
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
     *
     * @param tableName ajust table
     * @param colName   adjust column
     * @param newLevel  new level
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

    @Override
    public void initDatabase() {
        createConfigTable();
        Database database = manager.getDatabase();
        IvCipher randomCipher = new AESCipher(Mode.CBC, Padding.PKCS5);
        OPECipher opeCipher = new BoldyrevaCipher();
        database.getTables().forEach(table -> {
            int colNum = table.getColumns().size();
            List<String> ivs = new ArrayList<>();
            List<List<String>> cols = new ArrayList<>();
            table.getColumns().forEach(column -> {
                // create table
                List<String> col = new ArrayList<>();
                col.add(column.getColumnName());
                if (column.isVarchar()) {
                    String iv = base64Encode(randomCipher.generateIv());
                    ivs.add(iv);
                    manager.executeUpdate(String.format("INSERT INTO config VALUES (%s, %s, %s, %s)",
                            wrapQuote(table.getTableName()), wrapQuote(column.getColumnName()),
                            wrapQuote(iv), wrapQuote(Level.RANDOM.toString())));
                    try {
                        keyStoreWrapper.set(toKeyAlias(table.getTableName(), column.getColumnName(), Level.RANDOM),
                                (SecretKey) randomCipher.toKey(randomCipher.generateKey()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    col.add("VARCHAR(300)");
                } else if (column.isInt()) {
                    String iv = base64Encode(opeCipher.generateIv());
                    ivs.add(iv);
                    manager.executeUpdate(String.format("INSERT INTO config VALUES (%s, %s, %s, %s)",
                            wrapQuote(table.getTableName()), wrapQuote(column.getColumnName()),
                            wrapQuote(iv), wrapQuote(Level.ORDER.toString())));
                    try {
                        keyStoreWrapper.set(toKeyAlias(table.getTableName(), column.getColumnName(), Level.ORDER),
                                (SecretKey) opeCipher.toKey(opeCipher.generateKey()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    col.add("INT");
                } else {
                    throw new RuntimeException("Cannot handle more datatype as: " + column.getColumnType());
                }
                cols.add(col);
            });
            String createSql = "CREATE TABLE IF NOT EXISTS " + table.getTableName() + "_E(id_E INT AUTO_INCREMENT, " +
                    cols.stream()
                            .map(col -> String.join(" ", col))
                            .collect(Collectors.joining(", ")) +
                    ", PRIMARY KEY(id_E)) ENGINE=INNODB";
            manager.executeUpdate(createSql);

            // insert values
            List<List<String>> vals = new ArrayList<>();
            manager.getConnection().ifPresent(conn -> {
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s", table.getTableName()));
                    while (rs.next()) {
                        List<String> val = new ArrayList<>();
                        for (int i = 1; i <= colNum; i++) {
                            String plainText = rs.getString(i);
                            byte[] iv = base64Decode(ivs.get(i - 1));
                            table.getColumn(i - 1).ifPresent(column -> {
                                if (column.isVarchar()) {
                                    keyStoreWrapper.get(toKeyAlias(table.getTableName(),
                                            column.getColumnName(), Level.RANDOM)).ifPresent(entry -> {
                                        byte[] key = ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded();
                                        try {
                                            String encryptedVal = base64Encode(
                                                    randomCipher.encrypt(plainText.getBytes(), key, iv));
                                            val.add(wrapQuote(encryptedVal));
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            System.exit(1);
                                        }
                                    });
                                } else if (column.isInt()) {
                                    keyStoreWrapper.get(toKeyAlias(table.getTableName(),
                                            column.getColumnName(), Level.ORDER)).ifPresent(entry -> {
                                        byte[] key = ((KeyStore.SecretKeyEntry) entry).getSecretKey().getEncoded();
                                        try {
                                            String encryptedVal = opeCipher.encrypt(
                                                    new BigInteger(plainText), key, iv).toString();
                                            val.add(encryptedVal);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            System.exit(1);
                                        }
                                    });
                                } else {
                                    throw new RuntimeException("Cannot handle more datatype as: " + column.getColumnType());
                                }
                            });
                        }
                        vals.add(val);
                    }
                } catch (SQLException ex) {
                    manager.printSQLException(ex);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

                String insertSql = "INSERT INTO " + table.getTableName() + "_E(" +
                        table.getColumns().stream().map(Column::getColumnName).collect(Collectors.joining(", "))
                        + ") VALUES " +
                        vals.stream()
                                .map(val -> "(" + String.join(", ", val) + ")")
                                .collect(Collectors.joining(", "));
                manager.executeUpdate(insertSql);
            });
        });
    }

    /**
     * Handle RANDOM -> EQUALITY, RANDOM -> ORDER
     */
    private void adjustRandomTo(Level newLevel, byte[] key, byte[] iv, String tableName, String colName) {
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(String.format("SELECT id_E, %s FROM %s", colName, tableName));
                while (rs.next()) {
                    String id = rs.getString(1);
                    String val = rs.getString(2);
                    byte[] decryptedVal = randomCipher.decrypt(base64Decode(val), key, iv);
                    switch (newLevel) {
                        case EQUALITY:
                            updateLevel(tableName, colName, Level.EQUALITY);
                            byte[] newKey1 = determineCipher.generateKey();
                            keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                                    (SecretKey) determineCipher.toKey(newKey1));
                            String newVal1 = base64Encode(determineCipher.encrypt(decryptedVal, newKey1, iv));
                            updateColwithId(tableName, colName, id, newVal1);
                            break;
                        case ORDER:
                            updateLevel(tableName, colName, Level.ORDER);
                            byte[] newKey2 = opeCipher.generateKey();
                            keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                                    (SecretKey) opeCipher.toKey(newKey2));
                            BigInteger newVal2 = opeCipher.encrypt(new BigInteger(new String(decryptedVal)), key, iv);
                            updateColwithId(tableName, colName, id, newVal2.toString());
                            break;
                        default:
                            break;
                    }
                }
            } catch (SQLException ex) {
                manager.printSQLException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    /**
     * Handle EQUALITY -> ORDER
     */
    private void adjustEqualityTo(Level newLevel, byte[] key, byte[] iv, String tableName, String colName) {
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(String.format("SELECT id_E, %s FROM %s", colName, tableName));
                while (rs.next()) {
                    String id = rs.getString(1);
                    String val = rs.getString(2);
                    byte[] decryptedVal = determineCipher.decrypt(base64Decode(val), key, iv);
                    if (newLevel == Level.ORDER) {
                        byte[] newKey = opeCipher.generateKey();
                        keyStoreWrapper.set(toKeyAlias(tableName, colName, newLevel),
                                (SecretKey) opeCipher.toKey(newKey));
                        BigInteger newVal = opeCipher.encrypt(new BigInteger(new String(decryptedVal)), key, iv);
                        updateColwithId(tableName, colName, id, newVal.toString());
                    }
                }
            } catch (SQLException ex) {
                manager.printSQLException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private void initConfigTable() {
        configMap = new HashMap<>();
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(SELECT_CONFIG);
                while (rs.next()) {
                    List<String> col = new ArrayList<>();
                    String tableName = rs.getString(1);
                    String colName = rs.getString(2);
                    String iv = rs.getString(3);
                    String level = rs.getString(4);
                    col.add(colName);
                    col.add(iv);
                    col.add(level);
                    configMap.put(tableName, col);
                }
            } catch (SQLException ex) {
                manager.printSQLException(ex);
                System.exit(1);
            }
        });
    }

    private HashMap<String, HashMap<String, Level>> initLevelsInfo() {
        HashMap<String, HashMap<String, Level>> levels = new HashMap<>();
        for (Map.Entry<String, List<String>> e : configMap.entrySet()) {
            HashMap<String, Level> val = new HashMap<>();
            // 0 is column name, 2 is level
            val.put(e.getValue().get(0), Level.valueOf(e.getValue().get(2)));
            levels.put(e.getKey(), val);
        }
        return levels;
    }

    private HashMap<String, List<String>> initTablesInfo() {
        Database database = manager.getDatabase();
        HashMap<String, List<String>> tables = new HashMap<>();
        for (Table t : database.getTables()) {
            List<String> cols = new ArrayList<>();
            for (Column c : t.getColumns()) {
                cols.add(c.getColumnName());
            }
            tables.put(t.getTableName(), cols);
        }
        return tables;
    }

    private HashMap<String, HashMap<String, String>> initIvsInfo() {
        HashMap<String, HashMap<String, String>> ivs = new HashMap<>();
        for (Map.Entry<String, List<String>> e : configMap.entrySet()) {
            HashMap<String, String> val = new HashMap<>();
            // 1 is iv
            val.put(e.getValue().get(0), e.getValue().get(1));
            ivs.put(e.getKey(), val);
        }
        return ivs;
    }

    /**
     * Update level
     *
     * @param tableName table name
     * @param colName   column name
     * @param level     column level
     */
    private void updateLevel(String tableName, String colName, Level level) {
        String sql = String.format("UPDATE config set level=%s WHERE tableName=%s AND columnName=%s", level, tableName, colName);
        manager.executeUpdate(sql);
    }

    /**
     * Update initial vector
     *
     * @param tableName table name
     * @param colName   column name
     * @param iv        initial vector
     */
    private void updateIv(String tableName, String colName, String iv) {
        String sql = String.format("UPDATE config set iv=%s WHERE tableName=%s AND columnName=%s", iv, tableName, colName);
        manager.executeUpdate(sql);
    }

    private void updateColwithId(String tableName, String colName, String id, String newVal) {
        manager.executeUpdate(String.format("UPDATE %s SET %s=%s WHERE id=%s",
                tableName, colName, newVal, id));
    }

    private void createConfigTable() {
        manager.executeUpdate(CREATE_CONFIG);
    }

}
