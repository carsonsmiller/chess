package client;

public class ServerFacadeException extends Exception {
    private final int statusCode;

    public ServerFacadeException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ServerFacadeException(String message, Throwable cause) {
        super (message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}