package chris.seProxy;

import chris.seProxy.proxy.datasource.DataSourceManager;
import chris.seProxy.rewriter.Rewriter;
import chris.seProxy.security.scheme.SecurityScheme;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class Interpreter {
    private SecurityScheme scheme;
    private Rewriter rewriter;
    private DataSourceManager manager;

    Interpreter(DataSourceManager manager, SecurityScheme scheme, Rewriter rewriter) {
        this.manager = manager;
        this.scheme = scheme;
        this.rewriter = rewriter;
    }

    void interpret(String s) {
            rewriter.rewrite(s).ifPresent(rewriteSql ->
                    manager.getConnection().ifPresent(conn -> {
                try (Statement stmt = conn.createStatement()) {
                    if (stmt.execute(rewriteSql)) {
                        List<List<String>> table = new ArrayList<>();
                        log.info("execute query: " + rewriteSql);
                        ResultSet rs = stmt.getResultSet();
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNum = rsmd.getColumnCount();
                        while (rs.next()) {
                            List<String> row = new ArrayList<>();
                            for (int i = 1; i <= colNum; i++) {
                                String val = rs.getString(i);
                                row.add(scheme.decrypt(rsmd.getTableName(i).replace("_E", ""),
                                        rsmd.getColumnName(i), val));
                            }
                            table.add(row);
                        }
                        printQuery(table);
                    } else {
                        printUpdate(stmt.getUpdateCount());
                    }
                } catch (SQLException ex) {
                    manager.printSQLException(ex);
                }
            }));
    }

    private void printQuery(List<List<String>> table) {
        table.stream()
                .map(row -> String.join(" ", row))
                .forEach(System.out::println);
        System.out.println(table.size() + " rows in set");
    }

    private void printUpdate(int count) {
        System.out.println("Query OK, " + count + " rows affected");
    }
}
