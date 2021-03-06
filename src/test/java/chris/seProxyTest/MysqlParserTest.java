package chris.seProxyTest;

import chris.seProxy.parser.ParserWrapper;
import chris.seProxy.parser.mysql.MySqlLexer;
import chris.seProxy.parser.mysql.MySqlParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;

public class MysqlParserTest {
    private static final ParserWrapper parserWrapper = new ParserWrapper(MySqlLexer.class, MySqlParser.class);

    private static void parser(CharStream input) throws Exception {
        parserWrapper.init(input);
        MySqlParser parser = (MySqlParser) parserWrapper.getParser();
        ParseTree tree = parser.root();
        System.out.println(tree.toStringTree(parser));
    }

    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromString(
                "select * from product where name = \"asd\"".toUpperCase());
        parser(input);
    }
}
