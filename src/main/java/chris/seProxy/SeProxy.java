package chris.seProxy;

import chris.seProxy.proxy.datasource.OPEDatasourceManager;
import chris.seProxy.rewriter.Rewriter;
import chris.seProxy.rewriter.mysql.MysqlRewriter;
import chris.seProxy.security.scheme.OPEScheme;
import chris.seProxy.security.scheme.SecurityScheme;
import chris.seProxy.util.PropManager;

import java.util.Scanner;

public class SeProxy {
    private static final PropManager manager = new PropManager();
    private static final OPEDatasourceManager dataSourceManager = new OPEDatasourceManager(manager);
    private static final SecurityScheme scheme = new OPEScheme(manager);
    private static final Rewriter rewriter = new MysqlRewriter(scheme);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("#> ");
        Interpreter interpreter = new Interpreter(dataSourceManager, scheme, rewriter);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            interpreter.interpret(line);
            System.out.print("#> ");
        }
    }
}
