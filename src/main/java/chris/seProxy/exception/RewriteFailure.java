package chris.seProxy.exception;

/**
 * Exception for rewrite failure
 */
public class RewriteFailure extends RuntimeException {

    public RewriteFailure(String msg) {
        super("Rewrite failed: " + msg);
    }

}
