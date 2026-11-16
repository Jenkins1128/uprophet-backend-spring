package com.uprophet.common.error;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
