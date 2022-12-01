package com.punchin.enums;

public enum ClaimDataFilter {
    ALL(1, "All"),
    PENDING(2, "Pending"),
    WIP(3, "WIP"),
    SETTLED(4, "Settled");

    private final String value;

    private final int key;

    private ClaimDataFilter(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
