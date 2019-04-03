package chris.seProxy.rewriter.context;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Specific context for insert statement
 */
public class InsertStatementContext {
    /**
     * Always has a column list
     */
    @Setter
    private List<String> cols;
    @Getter @Setter
    private int currentIndex;

    public InsertStatementContext(List<String> cols) {
        this.cols = cols;
        currentIndex = 0;
    }

    public String getCurrentCol() {
        return cols.get(currentIndex);
    }

    public void inc() {
        currentIndex++;
    }

    public void clear() {
        cols = null;
        currentIndex = 0;
    }
}
