package chris.seProxy.rewriter.mysql;

import chris.seProxy.security.SecurityScheme;
import chris.seProxy.sql.parser.mysql.MySqlParser;
import chris.seProxy.sql.parser.mysql.MySqlParserBaseListener;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void enterCreateDatabase(MySqlParser.CreateDatabaseContext ctx) {
        String dbName = ctx.uid().getText();
        rewriter.replace(ctx.uid().getStart(), scheme.encryptDatabaseName(dbName));
        ctx.uid().getText();
    }

    @Override
    public void enterCreateIndex(MySqlParser.CreateIndexContext ctx) {
        String tableName = ctx.tableName().getText();
        List<MySqlParser.IndexColumnNameContext> indexColumnNames = ctx.indexColumnNames().indexColumnName();
        rewriter.replace(ctx.tableName().getStart(), scheme.encryptTableName(tableName));
        indexColumnNames.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptColumnName(tableName, ctx1.getText())));
    }
}
