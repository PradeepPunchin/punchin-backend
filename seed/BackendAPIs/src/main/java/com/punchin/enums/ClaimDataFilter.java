package com.punchin.enums;

public enum ClaimDataFilter {
    DRAFT(1, "DRAFT"),
    SUBMITED(2, "SUBMITED");

    private final String value;

    private final int key;

    private ClaimDataFilter(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
