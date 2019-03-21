package chris.seProxy.sql.ast;

public class EmptyStatementNode implements AST {
    private static EmptyStatementNode ourInstance = new EmptyStatementNode();

    public static EmptyStatementNode getInstance() {
        return ourInstance;
    }

    private EmptyStatementNode() {
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
