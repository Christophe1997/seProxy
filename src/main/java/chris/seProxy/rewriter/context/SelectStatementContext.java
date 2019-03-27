package chris.seProxy.rewriter.context;

import chris.seProxy.security.Property;
import chris.seProxy.util.Assoc;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class SelectStatementContext {

    private Assoc<String, String> tableAlias;

    @Setter @Getter
    private Property property;

    public SelectStatementContext() {
        tableAlias = new Assoc<>();
    }

    public void addTable(String tableName, String alias) {
        tableAlias.add(tableName, alias);
    }

    public Optional<String> getTableAlias(String tableName) {
        return tableAlias.findSnd(tableName).flatMap(s -> {
            if (s.equals("")) return Optional.empty();
            else return Optional.of(s);
        });
    }

    public Optional<String> getTableName(String alias) {
        return tableAlias.findFst(alias);
    }

}
