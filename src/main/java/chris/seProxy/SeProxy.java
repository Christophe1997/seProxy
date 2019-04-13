package chris.seProxy;

import chris.seProxy.parser.ParserWrapper;
import chris.seProxy.parser.mysql.MySqlLexer;
import chris.seProxy.parser.mysql.MySqlParser;
import chris.seProxy.rewriter.Rewriter;
import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.scheme.OPEScheme;
import chris.seProxy.security.scheme.SecurityScheme;
import chris.seProxy.util.PropManager;

import java.util.Scanner;

public class SeProxy {
    private static final PropManager manager = new PropManager();
    private static final ParserWrapper parser = new ParserWrapper(MySqlLexer.class, MySqlParser.class);
    private static final SecurityScheme scheme = new OPEScheme(manager);
    private static final Rewriter rewriter = new MysqlRewriter(parser, scheme);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.out.println(line);
        }
    }
}
