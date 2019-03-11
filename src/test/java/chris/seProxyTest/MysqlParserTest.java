package chris.seProxyTest;

import chris.seProxy.sql.parser.mysql.MySqlLexer;
import chris.seProxy.sql.parser.mysql.MySqlParser;
import chris.seProxy.util.ParserWrapper;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MysqlParserTest {
    private static final ParserWrapper parserWrapper = new ParserWrapper(MySqlLexer.class, MySqlParser.class);

    public static void parser(CharStream input) throws Exception {
        parserWrapper.init(input);
        MySqlParser parser = (MySqlParser) parserWrapper.getParser();
        ParseTree tree = parser.root();
        System.out.println(tree.toStringTree(parser));
    }

    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromString("alter table with_check add constraint c2 check (c1 in (1, 2, 3, 4));".toUpperCase());
        parser(input);
    }
}
