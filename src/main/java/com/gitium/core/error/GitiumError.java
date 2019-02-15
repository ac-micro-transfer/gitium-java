package com.gitium.core.error;

public enum GitiumError {
    INVALID_SEED("Invalid seed provided."),

    INVALID_INDEX("Invalid index provided."),

    INVALID_SECURITY_LEVEL("Invalid security level provided."),

    INVALID_ADDRESSES("Invalid addresses provided."),

    INVALID_TRANSFER_VALUE("Invalid transfer value."),

    INVALID_TRANSFERS("Invalid transfers provided."),

    NOT_ENOUGH_BALANCE("Not enough balance."),

    HAS_TRANSACTION_NOT_VERIFIED("Has transaction not verified."),

    INCONSISTENT_SECURITY_LEVEL_AND_TRANSACTIONS("Inconsistent security level and transactions"),

    INVALID_HASHES("Invalid hashes provided."),

    INVALID_TRYTES("Invalid trytes provided."),

    INVALID_ATTACHED_TRYTES("Invalid attached trytes provided.");

    private String message;

    GitiumError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}