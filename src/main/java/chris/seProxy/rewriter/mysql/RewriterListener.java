package chris.seProxy.rewriter.mysql;

import chris.seProxy.security.SecurityScheme;
import chris.seProxy.sql.parser.mysql.MySqlParserBaseListener;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

//TODO
public class RewriterListener extends MySqlParserBaseListener {
    @Getter @Setter
    private TokenStreamRewriter rewriter;
    @Getter @Setter
    private SecurityScheme scheme;

    public RewriterListener(TokenStream stream, SecurityScheme scheme) {
        rewriter = new TokenStreamRewriter(stream);
        this.scheme = scheme;
    }

}
