package chris.seProxy.rewriter;

/**
 * Exception for rewrite failure
 */
public class RewriteFailure extends RuntimeException {

    public RewriteFailure(String msg) {
        super("Rewrite failed: " + msg);
    }

}
