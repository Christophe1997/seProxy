package chris.seProxyTest;

import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.BaseScheme;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MysqlRewriterTest {

    static class TestScheme extends BaseScheme {

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
        public String encryptColumnName(String tableName, String colName) {
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
}
