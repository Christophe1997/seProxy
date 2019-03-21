package chris.seProxy.sql.ast.sqlStatement.ddlStatement;

import chris.seProxy.sql.ast.ASTVisitor;
import chris.seProxy.sql.ast.Identity;
import lombok.Getter;
import lombok.Setter;

public class CreateDatabaseOption implements DDLStatement {


    public enum Option {
        CHARSET, COLLATE;
    }

    @Getter @Setter
    private boolean isDefault;

    @Getter @Setter
    private Option option;

    @Getter @Setter
    private Identity name;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
