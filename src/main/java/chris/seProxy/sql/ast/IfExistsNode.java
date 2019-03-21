package chris.seProxy.sql.ast;

public class IfExistsNode implements SQLStatement {
    private static IfExistsNode ourInstance = new IfExistsNode();

    public static IfExistsNode getInstance() {
        return ourInstance;
    }

    private IfExistsNode() {
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
