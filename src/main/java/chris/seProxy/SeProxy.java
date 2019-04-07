package chris.seProxy;

import chris.seProxy.proxy.Agent;
import chris.seProxy.rewriter.Rewriter;

public class SeProxy {

    private Rewriter rewriter;
    private Agent agent;

    public void executeSQL(String sql) {
        rewriter.rewrite(sql).ifPresent(agent::execute);
    }
}
