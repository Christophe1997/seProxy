package chris.seProxy.exception;

/**
 * Exception for db connection failure
 */
public class ConnectionFailure extends Exception {
    public ConnectionFailure(String msg) {
        super("Driver not register: " + msg);
    }
}
