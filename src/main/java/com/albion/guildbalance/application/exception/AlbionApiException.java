package com.albion.guildbalance.application.exception;

public class AlbionApiException extends RuntimeException {

    public AlbionApiException(String message) {
        super(message);
    }

    public AlbionApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
