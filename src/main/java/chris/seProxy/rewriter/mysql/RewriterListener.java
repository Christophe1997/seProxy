package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.RewriteFailure;
import chris.seProxy.rewriter.context.Context;
import chris.seProxy.rewriter.context.InsertStatementContext;
import chris.seProxy.rewriter.context.SelectStatementContext;
import chris.seProxy.security.Level;
import chris.seProxy.security.scheme.SecurityScheme;
import chris.seProxy.parser.mysql.MySqlParser;
import chris.seProxy.parser.mysql.MySqlParserBaseListener;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.List;
import java.util.stream.Collectors;

// TODO more features
public class RewriterListener extends MySqlParserBaseListener {
    @Getter
    @Setter
    private TokenStreamRewriter rewriter;
    @Getter
    @Setter
    private SecurityScheme scheme;

    private Context context;

    private Context tempContext;

    RewriterListener(TokenStream stream, SecurityScheme scheme) {
        rewriter = new TokenStreamRewriter(stream);
        this.scheme = scheme;
        context = new Context();
    }

    // DML statement

    /**
     * While enter insert statement, such as {@code INSERT INTO t1(id, name) VALUES (2, "Bob"), (3, Alice)}.
     * To get the exact context for specific column, it is important to get the whole column that used in
     * the statememnt.
     */
    @Override
    public void enterInsertStatement(MySqlParser.InsertStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
        List<String> cols;
        if (ctx.columns != null) {
            cols = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
        } else {
            cols = scheme.middleware().getColsFromTable(
                    context.getCurrentTable().orElseThrow(() -> new RewriteFailure("no table on stack.")))
                    .orElseThrow(() -> new RewriteFailure("cols not found"));

        }

        context.setInsertStatementContext(new InsertStatementContext(cols));
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

    /**
     * FullColumnName is import to get the specific context, it always has table context, such as {@code table1.col1},
     * if not, it means the table context can get from earlier context. Now it only handle the select statement.
     */
    @Override
    public void enterFullColumnName(MySqlParser.FullColumnNameContext ctx) {
        if (!ctx.dottedId().isEmpty()) {
            if (context.getSelectStatementContext() != null) {
                context.getSelectStatementContext().getTableName(ctx.uid().getText()).ifPresent(
                        s -> context.setCurrentTable(s)
                );
            }
            context.setCurrentCol(ctx.dottedId(0).getText().substring(1));
        } else {
            if (context.getSelectStatementContext() != null) {
                context.setCurrentTable(context.getSelectStatementContext().getDefaultTable().orElseThrow(() ->
                        new RewriteFailure("no table declared")));
            }
            context.setCurrentCol(ctx.uid().getText());
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
    public void enterReplaceStatement(MySqlParser.ReplaceStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
        List<String> cols;
        if (ctx.columns != null) {
            cols = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
        } else {
            cols = scheme.middleware().getColsFromTable(
                    context.getCurrentTable().orElseThrow(() -> new RewriteFailure("no table on stack")))
                    .orElseThrow(() -> new RewriteFailure("cols not found"));
        }
        context.getInsertStatementContext().setCols(cols);
    }

    @Override
    public void exitReplaceStatement(MySqlParser.ReplaceStatementContext ctx) {
        context.clearCurrentTable();
        context.getInsertStatementContext().clear();
    }
    // Select


    @Override
    public void enterSimpleSelect(MySqlParser.SimpleSelectContext ctx) {
        context.setSelectStatementContext(new SelectStatementContext());
    }

    @Override
    public void enterParenthesisSelect(MySqlParser.ParenthesisSelectContext ctx) {
        context.setSelectStatementContext(new SelectStatementContext());
    }

    @Override
    public void exitSimpleSelect(MySqlParser.SimpleSelectContext ctx) {
        context.setSelectStatementContext(null);
    }

    @Override
    public void exitParenthesisSelect(MySqlParser.ParenthesisSelectContext ctx) {
        context.setSelectStatementContext(null);
    }

    @Override
    public void enterUnionSelect(MySqlParser.UnionSelectContext ctx) {
        context.setSelectStatementContext(new SelectStatementContext());
    }

    @Override
    public void exitUnionSelect(MySqlParser.UnionSelectContext ctx) {
        context.setSelectStatementContext(null);
    }

    @Override
    public void enterUnionParenthesisSelect(MySqlParser.UnionParenthesisSelectContext ctx) {
        context.setSelectStatementContext(new SelectStatementContext());
    }

    @Override
    public void exitUnionParenthesisSelect(MySqlParser.UnionParenthesisSelectContext ctx) {
        context.setSelectStatementContext(null);
    }

    @Override
    public void enterQuerySpecification(MySqlParser.QuerySpecificationContext ctx) {
        if (ctx.orderByClause() != null) {
            context.getSelectStatementContext().setLevel(Level.ORDER);
        }
    }

    @Override
    public void enterSelectStarElement(MySqlParser.SelectStarElementContext ctx) {
        context.setCurrentTable(ctx.fullId().getText());
    }

    /**
     * this is where table name and it's alias come from.
     */
    @Override
    public void enterAtomTableItem(MySqlParser.AtomTableItemContext ctx) {
        context.getSelectStatementContext().addTable(ctx.tableName().getText(),
                ctx.alias != null ? ctx.alias.getText() : ctx.tableName().getText());
    }

    // TODO support subqueryTableItem


    @Override
    public void enterLimitClause(MySqlParser.LimitClauseContext ctx) {
        tempContext = context;
        context.clearAll();
    }

    @Override
    public void exitLimitClause(MySqlParser.LimitClauseContext ctx) {
        if (tempContext == null) {
            throw new RewriteFailure("No context cached");
        } else {
            context = tempContext;
            tempContext = null;
        }
    }

    @Override
    public void enterInPredicate(MySqlParser.InPredicateContext ctx) {
        context.setCurrentLevel(Level.EQUALITY);
    }

    @Override
    public void exitInPredicate(MySqlParser.InPredicateContext ctx) {
        context.clearCurrentLevel();
    }

    /**
     * With binary comparison operator, the operations level can be determined.
     */
    @Override
    public void enterBinaryComparasionPredicate(MySqlParser.BinaryComparasionPredicateContext ctx) {
        if (context.getSelectStatementContext() != null) {
            String op = ctx.comparisonOperator().getText();
            if (op.equals("=") || op.equals("!=")) {
                context.setCurrentLevel(Level.EQUALITY);
            } else {
                context.setCurrentLevel(Level.ORDER);
            }
        }
    }

    @Override
    public void exitBinaryComparasionPredicate(MySqlParser.BinaryComparasionPredicateContext ctx) {
        context.clearCurrentLevel();
    }

    @Override
    public void enterBetweenPredicate(MySqlParser.BetweenPredicateContext ctx) {
        context.setCurrentLevel(Level.ORDER);
    }

    @Override
    public void exitBetweenPredicate(MySqlParser.BetweenPredicateContext ctx) {
        context.clearCurrentLevel();
    }

    @Override
    public void enterSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext ctx) {
        context.setCurrentLevel(Level.LIKE);
    }

    @Override
    public void exitSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext ctx) {
        context.clearCurrentLevel();
    }

    @Override
    public void enterLikePredicate(MySqlParser.LikePredicateContext ctx) {
        context.setCurrentLevel(Level.LIKE);
    }

    @Override
    public void exitLikePredicate(MySqlParser.LikePredicateContext ctx) {
        context.clearCurrentLevel();
    }

    // TODO support regexp


    @Override
    public void enterConstant(MySqlParser.ConstantContext ctx) {
        rewriter.replace(ctx.getStart(), ctx.getStop(),
                scheme.encrypt(context, ctx.getText()));
    }

}
