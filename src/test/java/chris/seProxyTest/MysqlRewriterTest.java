package chris.seProxyTest;

import chris.seProxy.rewriter.context.Context;
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
        public String encrypt(Context context, String val) {
            return testEncrypt(val);
        }
    }

    private static Optional<String> rewrite(String input) {
        MysqlRewriter rewriter = new MysqlRewriter(new TestScheme());
        return rewriter.rewrite(input);
    }

    private static void test(String input, String shouldOut) {
        rewrite(input).ifPresent(out -> assertEquals(shouldOut, out));
    }


    @Test
    public void insertStatementShouldPass() {
        String input = "INSERT INTO course SET id = 3 ON DUPLICATE KEY UPDATE name='asd'";
        String shouldOut = "not finished";
        test(input, shouldOut);

    }
}
