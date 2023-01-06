package com.punchin.enums;

public enum RemarkForEnum {
    AGENT(1, "Admin"),
    BANKER(2, "Banker"),
    VERIFIER(3, "Verifier");

    private final String value;

    private final int key;

    private RemarkForEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
