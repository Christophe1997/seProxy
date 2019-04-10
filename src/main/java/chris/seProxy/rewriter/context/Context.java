package chris.seProxy.rewriter.context;

import chris.seProxy.security.Level;
import lombok.Getter;
import lombok.Setter;

import java.util.EmptyStackException;
import java.util.Optional;
import java.util.Stack;

/**
 * Context during walk through the parse tree.
 */
public class Context {

    private Stack<String> tableStack;
    private Stack<String> colStack;
    private Stack<Level> levelStack;

    @Getter @Setter
    private InsertStatementContext insertStatementContext;

    @Getter @Setter
    private SelectStatementContext selectStatementContext;

    public Context() {
        tableStack = new Stack<>();
        colStack = new Stack<>();
        levelStack = new Stack<>();
    }

    public Optional<String> getCurrentTable() {
        try {
            return Optional.of(tableStack.peek());
        } catch (EmptyStackException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getCurrentCol() {
        try {
            return Optional.of(colStack.peek());
        } catch (EmptyStackException ex) {
            return Optional.empty();
        }
    }

    public Optional<Level> getCurrentLevel() {
        try {
            return Optional.of(levelStack.peek());
        } catch (EmptyStackException ex) {
            return Optional.empty();
        }
    }

    public void setCurrentTable(String tableName) {
        tableStack.push(tableName);
    }


    public void setCurrentCol(String colName) {
        colStack.push(colName);
    }

    public void setCurrentLevel(Level p) {
        levelStack.push(p);
    }

    public void clearCurrentTable() {
        tableStack.pop();
    }

    public void clearCurrentCol() {
        tableStack.pop();
    }

    public void clearCurrentLevel() {
        levelStack.pop();
    }

    public void clearAll() {
        tableStack.clear();
        colStack.clear();
        levelStack.clear();
        insertStatementContext = null;
        selectStatementContext = null;
    }


}
