package biz.dealnote.messenger.crypt;

public class InvalidSessionStateException extends Exception {

    public InvalidSessionStateException() {
    }

    public InvalidSessionStateException(String message) {
        super(message);
    }
}
