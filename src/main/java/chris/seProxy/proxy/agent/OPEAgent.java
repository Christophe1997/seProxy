package chris.seProxy.proxy.agent;

import chris.seProxy.db.Column;
import chris.seProxy.db.Database;
import chris.seProxy.db.Table;
import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.security.Level;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class OPEAgent implements Agent {
    private DataSourceManager manager;
    private Database database;

    private HashMap<String, List<String>> configMap;

    public OPEAgent(DataSourceManager manager) {
        this.manager = manager;
        try {
            database = Utils.initDatabaseInfo(manager);
            initConfigTable();
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }


    @Override
    public void executeUpdate(String sql) {
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                long start = System.currentTimeMillis();
                int i = stmt.executeUpdate(sql);
                long end = System.currentTimeMillis();
                printSQLWarnings(stmt.getWarnings());
                log.info(String.format("Query OK, %d row affected", i));
                log.info(String.format("Time: %.3ds", (end - start) / 1000));
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        });
    }

    @Override
    public ResultSet executeQuery(String sql) {
        AtomicReference<ResultSet> wrapper = new AtomicReference<>();
        manager.getConnection().ifPresent(conn -> {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                wrapper.set(rs);
            }catch (SQLException ex) {
                printSQLException(ex);
            }
        });
        return wrapper.get();
    }

    @Override
    public void printSQLException(SQLException ex) {
        while (ex != null) {
            log.error("SQLState: " + ex.getSQLState());
            log.error("Error Code: " + ex.getErrorCode());
            log.error("Message: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                log.error("Cause: " + t);
                t = t.getCause();
            }
            ex = ex.getNextException();
        }
    }

    @Override
    public void printSQLWarnings(SQLWarning warning) {
        while (warning != null) {
            log.warn("Message: " + warning.getMessage());
            log.warn("SQLState: " + warning.getSQLState());
            log.warn("Vendor error code: " + warning.getErrorCode());
            log.warn("");
            warning = warning.getNextWarning();
        }
    }

    private static final String SELECT_CONFIG = "SELECT config.table, config.column, config.iv, config.level FROM config";

    private void initConfigTable() throws SQLException {
        configMap = new HashMap<>();
        ResultSet rs = executeQuery(SELECT_CONFIG);
        while (rs.next()) {
            List<String> col = new ArrayList<>();
            String tableName = rs.getString(0);
            String colName = rs.getString(1);
            String iv = rs.getString(2);
            String level = rs.getString(3);
            col.add(colName); col.add(iv); col.add(level);
            configMap.put(tableName, col);
        }
    }

    public HashMap<String, HashMap<String, Level>> initLevelsInfo() {
        HashMap<String, HashMap<String, Level>> levels = new HashMap<>();
        for (Map.Entry<String, List<String>> e : configMap.entrySet()) {
            HashMap<String, Level> val = new HashMap<>();
            // 0 is column name, 2 is level
            val.put(e.getValue().get(0), Level.valueOf(e.getValue().get(2)));
            levels.put(e.getKey(), val);
        }
        return levels;
    }

    public HashMap<String, List<String>> initTablesInfo() {
        HashMap<String, List<String>> tables = new HashMap<>();
        for (Table t : database.tables()) {
            List<String> cols = new ArrayList<>();
            for (Column c : t.getColumns()) {
                cols.add(c.getColumnName());
            }
            tables.put(t.getTableName(), cols);
        }
        return tables;
    }

    public HashMap<String, HashMap<String, String>> initIvsInfo() {
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
     * @param level encryption level
     */
    public void updatelevel(String tableName, String colName, Level level) {
        String sql = String.format("UPDATE config set level=%s WHERE table=%s AND column=%s", level, tableName, colName);
        executeUpdate(sql);
    }

    /**
     * Update iv
     * @param iv encryption iv
     */
    public void updateIv(String tableName, String colName, String iv) {
        String sql = String.format("UPDATE config set iv=%s WHERE table=%s AND column=%s", iv, tableName, colName);
        executeUpdate(sql);
    }

}
