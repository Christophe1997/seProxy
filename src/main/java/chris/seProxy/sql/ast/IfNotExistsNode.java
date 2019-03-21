package chris.seProxy.sql.ast;

public class IfNotExistsNode implements SQLStatement {
    private static IfNotExistsNode ourInstance = new IfNotExistsNode();

    public static IfNotExistsNode getInstance() {
        return ourInstance;
    }

    private IfNotExistsNode() {
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
