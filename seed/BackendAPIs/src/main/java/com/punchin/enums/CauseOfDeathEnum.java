package com.punchin.enums;

public enum CauseOfDeathEnum {
    ACCIDENT(1, "ACCIDENT"),
    NATURAL_DEATH(2, "NATURAL_DEATH"),
    SUICIDE(3, "SUICIDE"),
    ILLNESS_MEDICAL_REASON(4, "ILLNESS_MEDICAL_REASON"),
    DUE_TO_NATURAL_CALAMITY(5, "DUE_TO_NATURAL_CALAMITY"),
    OTHER(6, "OTHER");

    private String value;
    private int key;

    private CauseOfDeathEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
