package chris.seProxy.rewriter.context;

import chris.seProxy.security.Property;
import chris.seProxy.util.Assoc;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Specific context for select statememt
 */
public class SelectStatementContext {

    /**
     * always has {@code AS} statement
     */
    private Assoc<String, String> tableAlias;

    /**
     * operation property
     */
    @Setter @Getter
    private Property property;

    public SelectStatementContext() {
        tableAlias = new Assoc<>();
    }

    public void addTable(String tableName, String alias) {
        tableAlias.add(tableName, alias);
    }

    public Optional<String> getTableAlias(String tableName) {
        return tableAlias.findSnd(tableName);
    }

    public Optional<String> getDefaultTable() {
        return tableAlias.getDefaultFst();
    }

    public Optional<String> getTableName(String alias) {
        return tableAlias.findFst(alias);
    }

}
