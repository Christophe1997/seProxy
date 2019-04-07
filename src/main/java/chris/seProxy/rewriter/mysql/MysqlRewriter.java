package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.Rewriter;
import chris.seProxy.security.scheme.BaseScheme;
import chris.seProxy.security.scheme.OPEScheme;
import chris.seProxy.security.scheme.SecurityScheme;
import chris.seProxy.parser.mysql.MySqlLexer;
import chris.seProxy.parser.mysql.MySqlParser;
import chris.seProxy.sql.parser.ParserWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Optional;

@AllArgsConstructor
public class MysqlRewriter implements Rewriter {

    @Getter @Setter
    private ParserWrapper parserWrapper;

    @Getter @Setter
    private SecurityScheme securityScheme;

    public MysqlRewriter() throws Exception {
        securityScheme = new BaseScheme();
        parserWrapper = new ParserWrapper(MySqlLexer.class, MySqlParser.class);
    }

    public MysqlRewriter(SecurityScheme scheme) {
        securityScheme = scheme;
        parserWrapper = new ParserWrapper(MySqlLexer.class, MySqlParser.class);
    }


    @Override
    public Optional<String> rewrite(String source) {
        return Optional.ofNullable(source).map(s -> {
            try {
                parserWrapper.init(CharStreams.fromString(s));
            } catch (Exception ex) {
                return null;
            }

            ParseTreeWalker walker = new ParseTreeWalker();
            RewriterListener listener = new RewriterListener(parserWrapper.getTokens(), securityScheme);
            ParseTree tree = ((MySqlParser) parserWrapper.getParser()).root();
            walker.walk(listener, tree);
            return listener.getRewriter().getText();
        });
    }
}
