package chris.seProxy.proxy;

import java.util.List;

public class BaseMiddleware implements Middleware {

    @Override
    public List<String> getColsFromTable(String tableName) {
        return null;
    }
}
