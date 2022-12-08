package com.punchin.enums;

public enum ClaimDataFilter {
    ALL(0, "ALL"),
    DRAFT(1, "DRAFT"),
    SUBMITTED(2, "SUBMITTED"),
    WIP(3, "WIP"),
    SETTLED(4, "SETTLED"),
    ALLOCATED(5, "ALLOCATED"),
    ACTION_PENDING(6, "ACTION_PENDING"),
    UNDER_VERIFICATION(6, "UNDER_VERIFICATION"),
    DISCREPENCY(7, "DISCREPENCY"),
    VERIFICATION_PENDING(8, "VERIFICATION_PENDING");

    private final String value;

    private final int key;

    private ClaimDataFilter(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
