package chris.seProxy.proxy;

import chris.seProxy.db.Column;
import chris.seProxy.db.Database;
import chris.seProxy.db.Table;
import chris.seProxy.proxy.agent.Agent;
import chris.seProxy.proxy.agent.BaseAgent;
import chris.seProxy.proxy.datasource.DataSourceManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Utils {

    public static <K, V> Optional<V> optionalGet(Map<K, V> map, K key) {
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(key));
    }

    public String allColumnsForCurrentDatabase() {
        return  "select * from information_schema.columns" +
                " where table_schema = DATABASE() order by table_name, ordinal_position";
    }

    public Database initDatabaseInfo(DataSourceManager manager) throws SQLException {
        Agent agent = new BaseAgent(manager);
        ResultSet rs = agent.executeQuery(allColumnsForCurrentDatabase());
        String dbName = null;
        HashMap<String, Table> tableMap = new HashMap<>();
        while (rs.next()) {
            String tableName = rs.getString(2);
            if (tableMap.get(tableName) == null) {
                String tableCatalog = rs.getString(0);
                String tableSchema = rs.getString(1);
                if (dbName == null) {
                    dbName = tableSchema;
                }
                tableMap.put(tableName, new Table(tableCatalog, tableSchema, tableName, new ArrayList<>()));
            }
            Table table = tableMap.get(tableName);
            Column column = new Column();
            column.setColumnName(rs.getString(3));
            column.setOrdinalPosition(rs.getInt(4));
            column.setColumnDefault(rs.getString(5));
            column.setNullable(rs.getBoolean(6));
            column.setDataType(rs.getString(7));
            column.setCharacterMaximumLength(rs.getString(8));
            column.setCharacterOctetLength(rs.getString(9));
            column.setNumericPrecision(rs.getString(10));
            column.setNumericScale(rs.getString(11));
            column.setDatetimePrecision(rs.getString(12));
            column.setCharacterSetName(rs.getString(13));
            column.setCollationName(rs.getString(14));
            column.setColumnType(rs.getString(15));
            column.setColumnKey(rs.getString(16));
            column.setExtra(rs.getString(17));
            column.setPrivileges(rs.getString(18));
            column.setColumnComment(rs.getString(19));
            column.setGenerationExpression(rs.getString(20));
            table.addColumn(column);
        }
        return new Database(dbName, tableMap);
    }
}
