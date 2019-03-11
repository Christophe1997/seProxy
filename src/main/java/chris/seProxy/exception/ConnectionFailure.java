package chris.seProxy.exception;

public class ConnectionFailure extends Exception {
    public ConnectionFailure(String msg) {
        super("Driver not register: " + msg);
    }
}
