package chris.seProxy.sql.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Identity implements AST {

    public enum Type {
        ID, FIELD_ID;
    }

    @Getter @Setter
    private Type type;

    @Getter @Setter
    private String stringLiteral;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
