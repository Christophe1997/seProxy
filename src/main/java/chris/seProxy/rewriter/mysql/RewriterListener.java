package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.context.Context;
import chris.seProxy.rewriter.context.InsertStatementContext;
import chris.seProxy.rewriter.context.SelectStatementContext;
import chris.seProxy.security.Property;
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

    // DML statement

    @Override
    public void enterInsertStatement(MySqlParser.InsertStatementContext ctx) {
        context.setCurrentTable(ctx.tableName().getText());
        List<String> cols;
        if (ctx.columns != null) {
            cols = ctx.columns.uid().stream().map(RuleContext::getText).collect(Collectors.toList());
        } else {
            cols = scheme.middleware().getColsFromTable(context.getCurrentTable());
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

    @Override
    public void enterFullColumnName(MySqlParser.FullColumnNameContext ctx) {
        if (!ctx.dottedId().isEmpty()) {
            context.setCurrentCol(ctx.dottedId(0).getText().substring(1));
        } else {
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
            cols = scheme.middleware().getColsFromTable(context.getCurrentTable());
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
            context.getSelectStatementContext().setProperty(Property.ORDER);
        }
    }

    @Override
    public void enterSelectStarElement(MySqlParser.SelectStarElementContext ctx) {
        context.setCurrentTable(ctx.fullId().getText());
    }

    @Override
    public void enterAtomTableItem(MySqlParser.AtomTableItemContext ctx) {
        context.getSelectStatementContext().addTable(ctx.tableName().getText(),
                ctx.alias != null ? ctx.alias.getText() : "");
    }

    // TODO support subqueryTableItem


    @Override
    public void enterInPredicate(MySqlParser.InPredicateContext ctx) {
        context.setCurrentProperty(Property.EQUALITY);
    }

    @Override
    public void exitInPredicate(MySqlParser.InPredicateContext ctx) {
        context.clearCurrentProperty();
    }

    @Override
    public void enterBinaryComparasionPredicate(MySqlParser.BinaryComparasionPredicateContext ctx) {
        if (context.getSelectStatementContext() != null) {
            String op = ctx.comparisonOperator().getText();
            if (op.equals("=") || op.equals("!=")) {
                context.setCurrentProperty(Property.EQUALITY);
            } else {
                context.setCurrentProperty(Property.ORDER);
            }
        }
    }

    @Override
    public void exitBinaryComparasionPredicate(MySqlParser.BinaryComparasionPredicateContext ctx) {
        context.clearCurrentProperty();
    }

    @Override
    public void enterBetweenPredicate(MySqlParser.BetweenPredicateContext ctx) {
        context.setCurrentProperty(Property.ORDER);
    }

    @Override
    public void exitBetweenPredicate(MySqlParser.BetweenPredicateContext ctx) {
        context.clearCurrentProperty();
    }

    @Override
    public void enterSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext ctx) {
        context.setCurrentProperty(Property.LIKE);
    }

    @Override
    public void exitSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext ctx) {
        context.clearCurrentProperty();
    }

    @Override
    public void enterLikePredicate(MySqlParser.LikePredicateContext ctx) {
        context.setCurrentProperty(Property.LIKE);
    }

    @Override
    public void exitLikePredicate(MySqlParser.LikePredicateContext ctx) {
        context.clearCurrentProperty();
    }

    // TODO support regexp


    @Override
    public void enterConstant(MySqlParser.ConstantContext ctx) {
        rewriter.replace(ctx.getStart(), ctx.getStop(),
                scheme.encrypt(context, ctx.getText()));
    }

}
