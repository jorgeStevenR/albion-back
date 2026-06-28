package com.albion.guildbalance.application.exception;

public class GuildNotFoundException extends RuntimeException {

    public GuildNotFoundException(String message) {
        super(message);
    }
}
