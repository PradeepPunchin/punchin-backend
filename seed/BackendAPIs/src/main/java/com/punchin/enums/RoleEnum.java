package com.punchin.enums;

public enum RoleEnum {
    ROLE_ADMIN(1, "Admin"),
    ROLE_BANKER(2, "Banker"),
    ROLE_VERIFIER(3, "Verifier"),
    ROLE_AGENT(4, "Agent");

    private final String value;

    private final int key;

    private RoleEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
