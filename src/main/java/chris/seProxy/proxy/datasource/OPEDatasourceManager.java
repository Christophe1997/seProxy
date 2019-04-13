package chris.seProxy.proxy.datasource;

import chris.seProxy.proxy.db.Column;
import chris.seProxy.proxy.db.Database;
import chris.seProxy.proxy.db.Table;
import chris.seProxy.util.PropManager;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class OPEDatasourceManager extends MysqlDataSourceManager {

    private Database database;


    public OPEDatasourceManager(PropManager manager) {
        super(manager);

        database = initDatabaseInfo();
    }

    public Database getDatabase() {
        return database;
    }

    private static final String ALL_COLUMNS_FOR_CURRENT_DATABASE =
            "select * from information_schema.columns" +
                    " where table_schema = DATABASE() order by table_name, ordinal_position";

    @NotNull
    private Database initDatabaseInfo() {
        HashMap<String, Table> tableMap = new HashMap<>();
        AtomicReference<String> dbName = new AtomicReference<>(null);
        getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(ALL_COLUMNS_FOR_CURRENT_DATABASE);
                while (rs.next()) {
                    String tableName = rs.getString(3);
                    if (tableMap.get(tableName) == null) {
                        String tableCatalog = rs.getString(1);
                        String tableSchema = rs.getString(2);
                        if (dbName.get() == null) {
                            dbName.set(tableSchema);
                        }
                        tableMap.put(tableName, new Table(tableCatalog, tableSchema, tableName, new ArrayList<>()));
                    }
                    Table table = tableMap.get(tableName);
                    Column column = new Column();
                    column.setColumnName(rs.getString(4));
                    column.setOrdinalPosition(rs.getInt(5));
                    column.setColumnDefault(rs.getString(6));
                    column.setNullable(Boolean.valueOf(rs.getString(7)));
                    column.setDataType(rs.getString(8));
                    column.setCharacterMaximumLength(rs.getString(9));
                    column.setCharacterOctetLength(rs.getString(10));
                    column.setNumericPrecision(rs.getString(11));
                    column.setNumericScale(rs.getString(12));
                    column.setDatetimePrecision(rs.getString(13));
                    column.setCharacterSetName(rs.getString(14));
                    column.setCollationName(rs.getString(15));
                    column.setColumnType(rs.getString(16));
                    column.setColumnKey(rs.getString(17));
                    column.setExtra(rs.getString(18));
                    column.setPrivileges(rs.getString(19));
                    column.setColumnComment(rs.getString(20));
                    column.setGenerationExpression(rs.getString(21));
                    table.addColumn(column);
                    tableMap.replace(tableName, table);
                }
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        });
        return new Database(dbName.get(), tableMap);
    }
}
