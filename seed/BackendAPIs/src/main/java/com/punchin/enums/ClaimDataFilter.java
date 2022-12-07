package com.punchin.enums;

public enum ClaimDataFilter {
    ALL(0, "ALL"),
    DRAFT(1, "DRAFT"),
    SUBMITTED(2, "SUBMITTED"),
    WIP(3, "WIP"),
    SETTLED(4, "SETTLED"),
    ALLOCATED(5, "ALLOCATED"),
    ACTION_PENDING(6, "ACTION_PENDING"),
    IN_PROGRESS(7, "IN_PROGRESS"),
    DISCREPENCY(8, "DISCREPENCY");

    private final String value;

    private final int key;

    private ClaimDataFilter(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
