package chris.seProxy.sql.ast;

public class EmptyStatementNode {
    private static EmptyStatementNode ourInstance = new EmptyStatementNode();

    public static EmptyStatementNode getInstance() {
        return ourInstance;
    }

    private EmptyStatementNode() {
    }
}
