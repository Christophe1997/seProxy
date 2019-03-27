package chris.seProxy.rewriter.context;

import chris.seProxy.security.Property;
import lombok.Getter;
import lombok.Setter;

import java.util.Stack;

public class Context {

    private Stack<String> tableStack;
    private Stack<String> colStack;
    private Stack<Property> propertyStack;

    @Getter @Setter
    private InsertStatementContext insertStatementContext;

    @Getter @Setter
    private SelectStatementContext selectStatementContext;

    public Context() {
        tableStack = new Stack<>();
        colStack = new Stack<>();
        propertyStack = new Stack<>();
    }

    public String getCurrentTable() {
        return tableStack.peek();
    }

    public String getCurrentCol() {
        return colStack.peek();
    }

    public Property getCurrentProperty() {
        return propertyStack.peek();
    }

    public void setCurrentTable(String tableName) {
        tableStack.push(tableName);
    }


    public void setCurrentCol(String colName) {
        colStack.push(colName);
    }

    public void setCurrentProperty(Property p) {
        propertyStack.push(p);
    }

    public void clearCurrentTable() {
        tableStack.pop();
    }

    public void clearCurrentCol() {
        tableStack.pop();
    }

    public void clearCurrentProperty() {
        propertyStack.pop();
    }

    public void clearAll() {
        tableStack.clear();
        colStack.clear();
        propertyStack.clear();
        insertStatementContext = null;
        selectStatementContext = null;
    }


}
