package chris.seProxy.rewriter.mysql;

import chris.seProxy.rewriter.RewriteVisitor;
import chris.seProxy.security.SecurityScheme;
import chris.seProxy.sql.ast.Identity;
import chris.seProxy.sql.ast.sqlStatement.ddlStatement.CreateDatabaseNode;
import chris.seProxy.sql.ast.sqlStatement.ddlStatement.CreateDatabaseOption;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static chris.seProxy.sql.ast.sqlStatement.ddlStatement.CreateDatabaseNode.DBFormat.DATABASE;

public class MysqlRewriter implements RewriteVisitor {

    private SecurityScheme scheme;

    @Override
    public String visit(CreateDatabaseNode node) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add("CREATE")
                .add(node.getDbFormat() == DATABASE ? "DATABASE" : "SCHEMA");
        if (node.isAllowExists()) {
            joiner.add("IF NOT EXIST");
        }
        joiner.add(scheme.encryptDatabaseName(visit(node.getUid())));
        for (CreateDatabaseOption option : node.getCreateDatabaseOptions()) {
            joiner.add(visit(option));
        }

        return joiner.toString();
    }

    @Override
    public String visit(Identity id) {
        return id.getStringLiteral();
    }

    @Override
    public String visit(CreateDatabaseOption option) {
        StringJoiner joiner = new StringJoiner(" ");
        if (option.isDefault()) {
            joiner.add("DEFAULT");
        }
        joiner.add(option.getOption() == CreateDatabaseOption.Option.CHARSET ? "CHARACTER SET" : "COLLATE")
                .add(visit(option.getName()));

        return joiner.toString();
    }
}
