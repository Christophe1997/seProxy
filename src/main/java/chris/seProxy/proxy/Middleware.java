package chris.seProxy.proxy;

import java.util.List;

public interface Middleware {

    List<String> getColsFromTable(String tableName);
}
