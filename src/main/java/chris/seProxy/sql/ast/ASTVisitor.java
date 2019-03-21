package chris.seProxy.sql.ast;

import chris.seProxy.sql.ast.sqlStatement.ddlStatement.CreateDatabaseNode;
import chris.seProxy.sql.ast.sqlStatement.ddlStatement.CreateDatabaseOption;

public interface ASTVisitor<T> {

    T visit(CreateDatabaseNode node);

    T visit(EmptyStatementNode node);

    T visit(IfNotExistsNode node);

    T visit(IfExistsNode node);

    T visit(CreateDatabaseOption option);

    T visit(Identity id);
}
