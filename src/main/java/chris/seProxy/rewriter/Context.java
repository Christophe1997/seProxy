package chris.seProxy.rewriter;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class Context {

    private Stack<String> tableStack;
    private Stack<String> colStack;

    @Getter @Setter
    private InsertStatementContext insertStatementContext;

    public Context() {
        tableStack = new Stack<>();
        colStack = new Stack<>();
    }

    public String getCurrentTable() {
        return tableStack.peek();
    }

    public String getCurrentCol() {
        return colStack.peek();
    }

    public void setCurrentTable(String tableName) {
        tableStack.push(tableName);
    }


    public void setCurrentCol(String colName) {
        colStack.push(colName);
    }

    public void clearCurrentTable() {
        tableStack.pop();
    }

    public void clearCurrentCol() {
        tableStack.pop();
    }

    public void clearAll() {
        tableStack.clear();
        colStack.clear();
    }


}
