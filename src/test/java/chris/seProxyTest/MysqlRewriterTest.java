package chris.seProxyTest;

import chris.seProxy.rewriter.context.Context;
import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.scheme.BaseScheme;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MysqlRewriterTest {

    static class TestScheme extends BaseScheme {

        TestScheme() {
            super();
        }

        @Override
        public String encrypt(Context context, String val) {

            StringBuilder builder = new StringBuilder();
            context.getCurrentTable().ifPresent(s -> builder.append(s).append("$"));
            context.getCurrentCol().ifPresent(s -> builder.append(s).append("$"));
            context.getCurrentProperty().ifPresent(s -> builder.append(s).append("$"));
            return builder.append(val).toString();

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
        String input1 = "INSERT INTO course SET course.id = 3 ON DUPLICATE KEY UPDATE name='asd'";
        String shouldOut1 = "INSERT INTO course SET course.id = course$id$3 ON DUPLICATE KEY UPDATE name=course$name$'asd'";
        test(input1, shouldOut1);

        String input2 = "INSERT INTO course(id, name) VALUES (1, 'a'), (2, 'b')";
        String shouldOut2 = "INSERT INTO course(id, name) VALUES (course$id$1, course$name$'a'), (course$id$2, course$name$'b')";
        test(input2, shouldOut2);
    }

    @Test
    public void selectStatementShouldPass() {
        String input1 = "SELECT * FROM table1 WHERE id=2";
        String input2 = "SELECT * FROM table1 WHERE table1.id>2";
        String input3 = "SELECT * FROM table1 AS t1 WHERE t1.id=2";
        String input4 = "SELECT t1.id AS e1, t2.id AS e2 FROM table1 AS t1, table2 AS t2" +
                " where t1.id > 2 and t2.id > 3 LIMIT 10";

        String shouldOut1 = "SELECT * FROM table1 WHERE id=table1$id$EQUALITY$2";
        test(input1, shouldOut1);

        String shouldOut2 = "SELECT * FROM table1 WHERE table1.id>table1$id$ORDER$2";
        test(input2, shouldOut2);

        String shouldOut3 = "SELECT * FROM table1 AS t1 WHERE t1.id=table1$id$EQUALITY$2";
        test(input3, shouldOut3);

        String shouldOut4 = "SELECT t1.id AS e1, t2.id AS e2 FROM table1 AS t1, table2 AS t2" +
                " where t1.id > table1$id$ORDER$2 and t2.id > table2$id$ORDER$3 LIMIT 10";
        test(input4, shouldOut4);
    }
}
