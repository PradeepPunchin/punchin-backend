package com.punchin.enums;

public enum RoleEnum {
    ADMIN(1, "Admin"),
    BANKER(2, "Banker"),
    VERIFIER(3, "Verifier"),
    AGENT(4, "Agent"),
    SUPER_BANKER(5, "SUPER_BANKER");

    private final String value;

    private final int key;

    private RoleEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
