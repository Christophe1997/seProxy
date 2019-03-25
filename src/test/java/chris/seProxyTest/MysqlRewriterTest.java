package chris.seProxyTest;

import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.BaseScheme;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MysqlRewriterTest {

    static class TestScheme extends BaseScheme {

        TestScheme() {
            super();
        }

        String testEncrypt(String name) {
            return name + "test";
        }

        @Override
        public String encryptDatabaseName(String name) {
            return testEncrypt(name);
        }

        @Override
        public String encryptTableName(String name) {
            return testEncrypt(name);
        }

        @Override
        public String encryptTableColumnName(String tableName, String colName) {
            return testEncrypt(colName);
        }

        @Override
        public String encryptViewName(String name) {
            return testEncrypt(name);
        }

        @Override
        public String encryptViewColumnName(String viewName, String colName) {
            return testEncrypt(colName);
        }
    }

    private static Optional<String> rewrite(String input) {
        MysqlRewriter rewriter = new MysqlRewriter(new TestScheme());
        return rewriter.rewrite(input);
    }

    private static void test(String input, String shouldOut) {
        rewrite(input).ifPresent(out -> {
            assertEquals(shouldOut, out);
        });
    }

    @Test
    public void createDatabaseShouldPass() {
        String input = "CREATE DATABASE TEST";
        String shouldOut = "CREATE DATABASE TESTtest";
        test(input, shouldOut);
    }

    @Test
    public void createIndexShouldPass() {
        String input = "CREATE UNIQUE INDEX INDEX1 ON TABLE1(COL1, COL2)";
        String shouldOut = "CREATE UNIQUE INDEX INDEX1 ON TABLE1test(COL1test, COL2test)";
        test(input, shouldOut);
    }

    @Test
    public void createTableShouldPass() {
        String input = "CREATE TEMPORARY TABLE table1 LIKE copyTable";
        String shouldOut = "CREATE TEMPORARY TABLE table1test LIKE copyTabletest";
        test(input, shouldOut);
    }

    @Test
    public void createViewShouldPass() {
        String input = "CREATE VIEW test.v(a, b) AS SELECT * FROM T";
        String shouldOut = "CREATE VIEW test.vtest(atest, btest) AS SELECT * FROM Ttest";
        test(input, shouldOut);
    }

    @Test
    public void insertStatementShouldPass() {
        String input = "INSERT INTO course SET id = 3 ON DUPLICATE KEY UPDATE name='asd'";
        String shouldOut = "not finished";
        test(input, shouldOut);

    }
}
