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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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

    private List<List<String>> configs;


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
        initLevelsAndIvsInfo();
        initTablesInfo();
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
                ResultSet rs = stmt.executeQuery(String.format("SELECT id_E, %s FROM %s_E", colName, tableName));
                switch (newLevel) {
                    case EQUALITY:
                        updateLevel(tableName, colName, Level.EQUALITY);
                        String alias1 = toKeyAlias(tableName, colName, newLevel);
                        byte[] newKey1;
                        if (!keyStoreWrapper.get(alias1).isPresent()) {
                            newKey1 = determineCipher.generateKey();
                            keyStoreWrapper.set(alias1,
                                    (SecretKey) determineCipher.toKey(newKey1));

                        } else {
                            newKey1 = ((KeyStore.SecretKeyEntry) keyStoreWrapper.get(alias1).get())
                                    .getSecretKey().getEncoded();
                        }
                        while (rs.next()) {
                            String id = rs.getString(1);
                            String val = rs.getString(2);
                            byte[] decryptedVal = randomCipher.decrypt(base64Decode(val), key, iv);
                            String newVal1 = base64Encode(determineCipher.encrypt(decryptedVal, newKey1));
                            updateColwithId(tableName, colName, id, newVal1);
                        }
                        break;
                    case ORDER:
                        updateLevel(tableName, colName, Level.ORDER);
                        String alias2 = toKeyAlias(tableName, colName, newLevel);
                        byte[] newKey2;
                        if (!keyStoreWrapper.get(alias2).isPresent()) {
                            newKey2 = opeCipher.generateKey();
                            keyStoreWrapper.set(alias2,
                                    (SecretKey) opeCipher.toKey(newKey2));
                        } else {
                            newKey2 = ((KeyStore.SecretKeyEntry) keyStoreWrapper.get(alias2).get())
                                    .getSecretKey().getEncoded();
                        }
                        while (rs.next()) {
                            String id = rs.getString(1);
                            String val = rs.getString(2);
                            byte[] decryptedVal = randomCipher.decrypt(base64Decode(val), key, iv);
                            BigInteger newVal2 = opeCipher.encrypt(new BigInteger(new String(decryptedVal)),
                                    newKey2, iv);
                            updateColwithId(tableName, colName, id, newVal2.toString());
                        }
                        break;
                    default:
                        break;
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
                if (newLevel == Level.ORDER) {
                    updateLevel(tableName, colName, Level.ORDER);
                    String alias = toKeyAlias(tableName, colName, newLevel);
                    byte[] newKey;
                    if (!keyStoreWrapper.get(alias).isPresent()) {
                        newKey = opeCipher.generateKey();
                        keyStoreWrapper.set(alias,
                                (SecretKey) opeCipher.toKey(newKey));
                    } else {
                        newKey = ((KeyStore.SecretKeyEntry) keyStoreWrapper.get(alias).get())
                                .getSecretKey().getEncoded();
                    }
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String val = rs.getString(2);
                        byte[] decryptedVal = determineCipher.decrypt(base64Decode(val), key, iv);
                        BigInteger newVal = opeCipher.encrypt(new BigInteger(new String(decryptedVal)), newKey, iv);
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
        configs = new ArrayList<>();
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(SELECT_CONFIG);
                while (rs.next()) {
                    List<String> col = new ArrayList<>();
                    col.add(rs.getString(1));  // tableName
                    col.add(rs.getString(2));  // colName
                    col.add(rs.getString(3));  // iv
                    col.add(rs.getString(4));  // level
                    configs.add(col);
                }
            } catch (SQLException ex) {
                manager.printSQLException(ex);
                System.exit(1);
            }
        });
    }

    private void initLevelsAndIvsInfo() {
        levels = new HashMap<>();
        ivs = new HashMap<>();
        configs.forEach(row -> {
            String tableName = row.get(0);
            String colName = row.get(1);
            String iv = row.get(2);
            String level = row.get(3);
            if (ivs.get(tableName) == null) {
                ivs.put(tableName, new HashMap<String, String>(){{
                    put(colName, iv);
                }});
            } else {
                HashMap<String, String> map = ivs.get(tableName);
                map.put(colName, iv);
                ivs.put(tableName, map);
            }

            if (levels.get(tableName) == null) {
                levels.put(tableName, new HashMap<String, Level>(){{
                    put(colName, Level.valueOf(level));
                }});
            } else {
                HashMap<String, Level> map = levels.get(tableName);
                map.put(colName, Level.valueOf(level));
                levels.put(tableName, map);
            }
        });
    }

    private void initTablesInfo() {
        Database database = manager.getDatabase();
        tables = new HashMap<>();
        for (Table t : database.getTables()) {
            List<String> cols = new ArrayList<>();
            for (Column c : t.getColumns()) {
                cols.add(c.getColumnName());
            }
            tables.put(t.getTableName(), cols);
        }
    }

    /**
     * Update level
     *
     * @param tableName table name
     * @param colName   column name
     * @param level     column level
     */
    private void updateLevel(String tableName, String colName, Level level) {
        String sql = String.format("UPDATE config set level=%s WHERE tableName=%s AND columnName=%s",
                wrapQuote(level.toString()), wrapQuote(tableName), wrapQuote(colName));
        levels.get(tableName).put(colName, level);
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
        String sql = String.format("UPDATE config set iv=%s WHERE tableName=%s AND columnName=%s",
                wrapQuote(iv), wrapQuote(tableName), wrapQuote(colName));
        ivs.get(tableName).put(colName, iv);
        manager.executeUpdate(sql);
    }

    private void updateColwithId(String tableName, String colName, String id, String newVal) {
        manager.executeUpdate(String.format("UPDATE %s_E SET %s=%s WHERE id_E=%s",
                tableName, colName, wrapQuote(newVal), id));
    }

    private void createConfigTable() {
        manager.executeUpdate(CREATE_CONFIG);
    }

}
