package com.albion.guildbalance.application.exception;

public class PlayerSyncException extends RuntimeException {

    public PlayerSyncException(String message) {
        super(message);
    }

    public PlayerSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
