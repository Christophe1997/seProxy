package chris.seProxy.proxy.agent;

import chris.seProxy.proxy.db.Column;
import chris.seProxy.proxy.db.Database;
import chris.seProxy.proxy.db.Table;
import chris.seProxy.proxy.Utils;
import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.Level;
import chris.seProxy.security.cipher.IvCipher;
import chris.seProxy.security.cipher.OPECipher;
import chris.seProxy.security.cipher.ciphers.AESCipher;
import chris.seProxy.security.cipher.ciphers.boldyreva.BoldyrevaCipher;
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
import java.util.stream.Collectors;

import static chris.seProxy.security.scheme.SecurityScheme.*;

@Slf4j
public class OPEAgent implements Agent {
    private DataSourceManager manager;
    private Database database;


    public OPEAgent(DataSourceManager manager) {
        this.manager = manager;
        try {
            database = Utils.initDatabaseInfo(manager);
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
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        });
        return wrapper.get();
    }

    @Override
    public Database getDatabase() {
        return database;
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





}
