package com.albion.guildbalance.domain.enums;

public enum ParticipantType {
    PLAYER(1.0),
    SCOUT(1.2),
    GUILD(1.0);

    private final double weight;

    ParticipantType(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
