package chris.seProxy.exception;

public class ParserInitFailure extends Exception {
    public ParserInitFailure(String msg) {
        super("Parser init failed: " + msg);
    }
}
