package chris.seProxy.parser;

/**
 * Exception for SQL parser init failure
 */
public class ParserInitFailure extends Exception {
    public ParserInitFailure(String msg) {
        super("Parser init failed: " + msg);
    }
}
