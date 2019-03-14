package chris.seProxy.exception;

public class SecurityMismatch extends Exception {

    public SecurityMismatch(String msg) {
        super("Security mismatch: " + msg);
    }
}
