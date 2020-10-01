package biz.dealnote.messenger.api;

import biz.dealnote.messenger.api.model.Error;


public class ApiException extends Exception {

    private final Error error;

    public ApiException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
