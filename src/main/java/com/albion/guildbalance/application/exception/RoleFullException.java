package com.albion.guildbalance.application.exception;

public class RoleFullException extends RuntimeException {

    public RoleFullException() {
        super("ROLE_FULL");
    }
}
