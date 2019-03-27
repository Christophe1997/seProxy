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
            return context.getCurrentTable() + "$" + context.getCurrentCol() + "$" + val;
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
    public void insertStatementSetShouldPass() {
        String input = "INSERT INTO course SET course.id = 3 ON DUPLICATE KEY UPDATE name='asd'";
        String shouldOut = "INSERT INTO course SET course.id = course$id$3 ON DUPLICATE KEY UPDATE name=course$name$'asd'";
        test(input, shouldOut);
    }

    @Test
    public void insertStatementValuesShouldPass() {
        String input = "INSERT INTO course(id, name) VALUES (1, 'a'), (2, 'b')";
        String shouldOut = "INSERT INTO course(id, name) VALUES (course$id$1, course$name$'a'), (course$id$2, course$name$'b')";
        test(input, shouldOut);
    }
}
