package chris.seProxy.sql.ast.sqlStatement.ddlStatement;

import chris.seProxy.sql.ast.ASTVisitor;
import chris.seProxy.sql.ast.Identity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class CreateDatabaseNode implements DDLStatement {

    /**
     * CREATE SCHEMA is a synonym for CREATE DATABASE
     */
    public enum DBFormat {
        DATABASE, SCHEMA;
    }

    @Getter @Setter
    private DBFormat dbFormat;

    /**
     * An error occurs if the database exists and you did not specify IF NOT EXISTS
     */
    @Getter @Setter
    private boolean allowExists;

    @Getter @Setter
    private Identity uid;

    @Getter @Setter
    private List<CreateDatabaseOption> createDatabaseOptions;

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
