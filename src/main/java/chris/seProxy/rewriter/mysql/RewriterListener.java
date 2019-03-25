package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.Context;
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

//    /**
//     * Encrypt data with specific table and column
//     *
//     * @param tableName table name
//     * @param colName   column name
//     */
//    private void replaceExpression(String tableName, String colName, @NotNull MySqlParser.ExpressionContext ctx) {
//        final String PREDICATE_EXP = "PredicateExpressionContext";
//        final String NOT_EXP = "NotExpressionContext";
//        final String LOGICAL_EXP = "LogicalExpressionContext";
//        final String IS_EXP = "IsExpressionContext";
//
//        switch (ctx.getClass().getSimpleName()) {
//            case NOT_EXP:
//                replaceExpression(tableName, colName, (MySqlParser.NotExpressionContext) ctx);
//                break;
//            case LOGICAL_EXP:
//                replaceExpression(tableName, colName, (MySqlParser.LogicalExpressionContext) ctx);
//                break;
//            case IS_EXP:
//                replaceExpression(tableName, colName, (MySqlParser.IsExpressionContext) ctx);
//                break;
//            case PREDICATE_EXP:
//                replaceExpression(tableName, colName, (MySqlParser.PredicateExpressionContext) ctx);
//                break;
//        }
//    }
//
//    private void replaceExpression(String tableName, String colName, @NotNull MySqlParser.NotExpressionContext ctx) {
//        replaceExpression(tableName, colName, ctx.expression());
//    }
//
//    private void replaceExpression(String tableName, String colName, @NotNull MySqlParser.LogicalExpressionContext ctx) {
//        ctx.expression().forEach(exp -> {
//            replaceExpression(tableName, colName, exp);
//        });
//    }
//
//    private void replaceExpression(String tableName, String colName, @NotNull MySqlParser.IsExpressionContext ctx) {
//        replacePredicate(tableName, colName, ctx.predicate());
//    }
//
//    private void replaceExpression(String tableName, String colName, @NotNull MySqlParser.PredicateExpressionContext ctx) {
//        replacePredicate(tableName, colName, ctx.predicate());
//    }
//
//    private void replacePredicate(String tableName, String colName, MySqlParser.PredicateContext ctx) {
//        final String IN_PREDICATE = "InPredicateContext";
//        final String IS_NULL_PREDICATE = "IsNullPredicateContext";
//        final String BINARY_COMPARASION_PREDICATE = "BinaryComparasionPredicateContext";
//        final String SUBQUERY_COMPARASION_PREDICATE = "SubqueryComparasionPredicateContext";
//        final String BETWEEN_PREDICATE = "BetweenPredicateContext";
//        final String SOUNDS_LIKE_PREDICATE = "SoundsLikePredicateContext";
//        final String LIKE_PREDICATE = "LikePredicateContext";
//        final String REGEXP_PREDICATE = "RegexpPredicateContext";
//        final String EXPRESSION_ATOM_PREDICATE = "ExpressionAtomPredicateContext";
//
//        switch (ctx.getClass().getSimpleName()) {
//            case IN_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.InPredicateContext) ctx);
//                break;
//            case IS_NULL_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.IsNullPredicateContext) ctx);
//                break;
//            case BINARY_COMPARASION_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.BinaryComparasionPredicateContext) ctx);
//                break;
//            case SUBQUERY_COMPARASION_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.SubqueryComparasionPredicateContext) ctx);
//                break;
//            case BETWEEN_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.BetweenPredicateContext) ctx);
//                break;
//            case SOUNDS_LIKE_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.SoundsLikePredicateContext) ctx);
//                break;
//            case LIKE_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.LikePredicateContext) ctx);
//                break;
//            case REGEXP_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.RegexpPredicateContext) ctx);
//                break;
//            case EXPRESSION_ATOM_PREDICATE:
//                replacePredicate(tableName, colName, (MySqlParser.ExpressionAtomPredicateContext) ctx);
//                break;
//        }
//
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.InPredicateContext ctx) {
//        replacePredicate(tableName, colName, ctx.predicate());
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.IsNullPredicateContext ctx) {
//        replacePredicate(tableName, colName, ctx.predicate());
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.BinaryComparasionPredicateContext ctx) {
//        ctx.predicate().forEach(p -> replacePredicate(tableName, colName, p));
//    }
//
//    private void replacePredicate(String tableName, String colName, MySqlParser.SubqueryComparasionPredicateContext ctx) {
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.BetweenPredicateContext ctx) {
//        ctx.predicate().forEach(p -> replacePredicate(tableName, colName, p));
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.SoundsLikePredicateContext ctx) {
//        ctx.predicate().forEach(p -> replacePredicate(tableName, colName, p));
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.LikePredicateContext ctx) {
//        ctx.predicate().forEach(p -> replacePredicate(tableName, colName, p));
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.RegexpPredicateContext ctx) {
//        ctx.predicate().forEach(p -> replacePredicate(tableName, colName, p));
//    }
//
//    private void replacePredicate(String tableName, String colName, @NotNull MySqlParser.ExpressionAtomPredicateContext ctx) {
//        replaceExpressionAtom(tableName, colName, ctx.expressionAtom());
//    }
//
//    private void replaceExpressionAtom(String tableName, String colName, MySqlParser.ExpressionAtomContext ctx) {
//        final String CONSTANT_EXPRESSION_ATOM = "ConstantExpressionAtomContext";
//        final String FULL_COLUMN_NAME_EXPRESSION_ATOM = "FullColumnNameExpressionAtomContext";
//    }

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
        String tableName = ctx.tableName().getText();
        List<MySqlParser.IndexColumnNameContext> indexColumnNames = ctx.indexColumnNames().indexColumnName();

        indexColumnNames.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptTableColumnName(tableName, ctx1.getText())));
    }

    /**
     * Encrypt table name whether or not it is temporary.
     */
    @Override
    public void enterTableName(MySqlParser.TableNameContext ctx) {
        rewriter.replace(ctx.getStart(), scheme.encryptTableName(ctx.getText()));
    }

    /**
     * CREATE VIEW
     * Encrypt view name.
     */
    @Override
    public void enterCreateView(MySqlParser.CreateViewContext ctx) {
        String viewName = ctx.fullId().getText();
        List<MySqlParser.UidContext> cols = ctx.uidList().uid();

        replaceFullId(ctx.fullId(), scheme::encryptViewName);
        cols.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptViewColumnName(viewName, ctx1.getText())));
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
        String viewName = ctx.fullId().getText();
        List<MySqlParser.UidContext> cols = ctx.uidList().uid();

        replaceFullId(ctx.fullId(), scheme::encryptViewName);
        cols.forEach(ctx1 ->
                rewriter.replace(ctx1.getStart(), scheme.encryptViewColumnName(viewName, ctx1.getText())));
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
        String tableName = ctx.tableName().getText();

        if (ctx.SET() != null) {
            ctx.updatedElement().forEach(ctx1 -> {
                String colName = ctx1.fullColumnName().getText();
                rewriter.replace(ctx1.fullColumnName().getStart(), ctx1.fullColumnName().getStop(),
                        scheme.encryptViewColumnName(tableName, colName));
                if (ctx1.expression() != null) {
                    replaceExpression(tableName, colName, ctx1.expression());
                }
            });
        } else {
            List<String> colsName;
            if (ctx.columns != null) {
                colsName = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
            } else {
                colsName = scheme.middleware().getColsFromTable(tableName);
            }
            MySqlParser.InsertStatementValueContext valuesCtx = ctx.insertStatementValue();
            if (valuesCtx.selectStatement() == null) {
                valuesCtx.expressionsWithDefaults().forEach(rowCtx -> {
                    List<MySqlParser.ExpressionOrDefaultContext> colsCtx = rowCtx.expressionOrDefault();
                    int index = 0;
                    for (MySqlParser.ExpressionOrDefaultContext colCtx : colsCtx) {
                        if (colCtx.expression() != null) {
                            replaceExpression(tableName, colsName.get(index), colCtx.expression());
                        }
                        index++;
                    }
                });
            }
        }

    }

}
