package chris.seProxy.sql.ast;


public interface AST {

    <T> T accept(ASTVisitor<T> visitor);

}
