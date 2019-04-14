package chris.seProxyTest;

import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.scheme.OPEScheme;
import chris.seProxy.security.scheme.SecurityScheme;
import chris.seProxy.util.PropManager;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OPESchemeTest {
    private static Optional<String> rewrite(String input) {
        SecurityScheme scheme = new OPEScheme(new PropManager());
        MysqlRewriter rewriter = new MysqlRewriter(scheme);
        return rewriter.rewrite(input);
    }

    private static void test(String input, String shouldOut) {
        rewrite(input).ifPresent(out -> assertEquals(shouldOut, out));
    }


    @Test
    public void rewriteShouldPass() {
//        System.out.println(rewrite("SELECT test.id FROM test WHERE name = 'Alice'").get());
        System.out.println(rewrite("INSERT INTO test VALUES (10, 'Cdfgb', 60), (4, 'dfg', 70)").get());
    }
}
