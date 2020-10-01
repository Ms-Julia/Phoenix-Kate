package biz.dealnote.messenger.exception;

import java.io.IOException;

public class UnauthorizedException extends IOException {

    public UnauthorizedException(String message) {
        super(message);
    }
}