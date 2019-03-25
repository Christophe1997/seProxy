package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.Context;
import chris.seProxy.rewriter.InsertStatementContext;
import chris.seProxy.security.SecurityScheme;
import chris.seProxy.sql.parser.mysql.MySqlParser;
import chris.seProxy.sql.parser.mysql.MySqlParserBaseListener;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO
public class RewriterListener extends MySqlParserBaseListener {
    @Getter
    @Setter
    private TokenStreamRewriter rewriter;
    @Getter
    @Setter
    private SecurityScheme scheme;

    private Context context;

    public RewriterListener(TokenStream stream, SecurityScheme scheme) {
        rewriter = new TokenStreamRewriter(stream);
        this.scheme = scheme;
        context = new Context();
    }

    // help function

    private void replaceUid(@NotNull MySqlParser.UidContext ctx, @NotNull Function<String, String> mapper) {
        rewriter.replace(ctx.getStart(), mapper.apply(ctx.getText()));
    }

    private void replaceFullId(@NotNull MySqlParser.FullIdContext ctx, @NotNull Function<String, String> mapper) {
        rewriter.replace(ctx.getStart(), ctx.getStop(), mapper.apply(ctx.getText()));
    }

    // DDL statement rewrite

    /**
     * CREATE DATABASE
     * Encrypt database name.
     */
    @Override
    public void enterCreateDatabase(MySqlParser.CreateDatabaseContext ctx) {
        replaceUid(ctx.uid(), scheme::encryptDatabaseName);
    }

    /**
     * CREATE INDEX
     * Encrypt column name with given table.
     */
    @Override
    public void enterCreateIndex(MySqlParser.CreateIndexContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
    }

    @Override
    public void exitCreateIndex(MySqlParser.CreateIndexContext ctx) {
        context.clearCurrentTable();
    }

    @Override
    public void enterIndexColumnName(MySqlParser.IndexColumnNameContext ctx) {
        replaceUid(ctx.uid(), col -> scheme.encryptColumnName(context, col));
    }


    /**
     * Encrypt table name whether or not it is temporary.
     */
    @Override
    public void enterTableName(MySqlParser.TableNameContext ctx) {
        replaceFullId(ctx.fullId(), scheme::encryptTableName);
    }

    /**
     * CREATE VIEW
     * Encrypt view name.
     */
    @Override
    public void enterCreateView(MySqlParser.CreateViewContext ctx) {
        context.setCurrentTable(ctx.fullId().getText());
        List<MySqlParser.UidContext> cols = ctx.uidList().uid();

        replaceFullId(ctx.fullId(), scheme::encryptViewName);
        cols.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptColumnName(context, ctx1.getText())));
    }

    @Override
    public void exitCreateView(MySqlParser.CreateViewContext ctx) {
        context.clearCurrentTable();
    }

    /**
     * The database name can be omitted, in which case the statement applies to the default database.
     */
    @Override
    public void enterAlterSimpleDatabase(MySqlParser.AlterSimpleDatabaseContext ctx) {
        if (ctx.uid() != null) {
            replaceUid(ctx.uid(), scheme::encryptDatabaseName);
        }
    }

    @Override
    public void enterAlterUpgradeName(MySqlParser.AlterUpgradeNameContext ctx) {
        replaceUid(ctx.uid(), scheme::encryptDatabaseName);
    }


    /**
     * Same as {@link #enterCreateView(MySqlParser.CreateViewContext)}
     */
    @Override
    public void enterAlterView(MySqlParser.AlterViewContext ctx) {
        context.setCurrentTable(ctx.fullId().getText());
        List<MySqlParser.UidContext> cols = ctx.uidList().uid();

        replaceFullId(ctx.fullId(), scheme::encryptViewName);
        cols.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptColumnName(context, ctx1.getText())));
    }

    @Override
    public void exitAlterView(MySqlParser.AlterViewContext ctx) {
        context.clearCurrentTable();
    }

    @Override
    public void enterDropDatabase(MySqlParser.DropDatabaseContext ctx) {
        replaceUid(ctx.uid(), scheme::encryptDatabaseName);
    }

    @Override
    public void enterDropView(MySqlParser.DropViewContext ctx) {
        List<MySqlParser.FullIdContext> viewNames = ctx.fullId();
        viewNames.forEach(ctx1 ->
                replaceFullId(ctx1, scheme::encryptViewName));
    }

    // DML statement rewrite


    @Override
    public void enterInsertStatement(MySqlParser.InsertStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
        List<String> cols;
        if (ctx.columns != null) {
            cols = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
        } else {
            cols = scheme.middleware().getColsFromTable(context.getCurrentTable());
        }
        context.getInsertStatementContext().setCols(cols);
    }

    @Override
    public void exitInsertStatement(MySqlParser.InsertStatementContext ctx) {
        context.clearCurrentTable();
        context.getInsertStatementContext().clear();
    }

    @Override
    public void enterExpressionsWithDefaults(MySqlParser.ExpressionsWithDefaultsContext ctx) {
        context.getInsertStatementContext().setCurrentIndex(0);
    }

    @Override
    public void enterExpressionOrDefault(MySqlParser.ExpressionOrDefaultContext ctx) {
        context.setCurrentCol(context.getInsertStatementContext().getCurrentCol());
    }

    @Override
    public void exitExpressionOrDefault(MySqlParser.ExpressionOrDefaultContext ctx) {
        context.getInsertStatementContext().inc();
    }

    @Override
    public void enterUpdatedElement(MySqlParser.UpdatedElementContext ctx) {
        context.setCurrentCol(ctx.fullColumnName().getText());
    }

    @Override
    public void exitUpdatedElement(MySqlParser.UpdatedElementContext ctx) {
        context.clearCurrentCol();
    }

    @Override
    public void enterConstant(MySqlParser.ConstantContext ctx) {
        rewriter.replace(ctx.getStart(), scheme.encrypt(context, ctx.getText()));
    }

    @Override
    public void enterFullColumnName(MySqlParser.FullColumnNameContext ctx) {
        if (ctx.dottedId() != null) {
            replaceUid(ctx.uid(), scheme::encryptTableName);
            ctx.dottedId().forEach(dottedId -> {
                rewriter.replace(dottedId.getStart(), dottedId.getStop(),
                        "." + scheme.encryptColumnName(context, dottedId.getText().substring(1)));
            });
        }
    }

    @Override
    public void enterLoadDataStatement(MySqlParser.LoadDataStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
    }

    @Override
    public void exitLoadDataStatement(MySqlParser.LoadDataStatementContext ctx) {
        context.clearCurrentTable();
    }

    @Override
    public void enterAssignmentField(MySqlParser.AssignmentFieldContext ctx) {
        if (ctx.uid() != null) {
            replaceUid(ctx.uid(), col -> scheme.encryptColumnName(context, col));
        }
    }


    @Override
    public void enterReplaceStatement(MySqlParser.ReplaceStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
        List<String> cols;
        if (ctx.columns != null) {
            cols = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
        } else {
            cols = scheme.middleware().getColsFromTable(context.getCurrentTable());
        }
        context.getInsertStatementContext().setCols(cols);
    }

    @Override
    public void exitReplaceStatement(MySqlParser.ReplaceStatementContext ctx) {
        context.clearCurrentTable();
        context.getInsertStatementContext().clear();
    }

    @Override
    public void enterSelectStarElement(MySqlParser.SelectStarElementContext ctx) {
        replaceFullId(ctx.fullId(), scheme::encryptTableName);
    }

}
