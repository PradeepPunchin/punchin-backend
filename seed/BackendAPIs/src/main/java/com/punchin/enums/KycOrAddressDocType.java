package com.punchin.enums;

public enum KycOrAddressDocType {
    AADHAR_CARD(1, "AADHAR_CARD"),
    PASSPORT(2, "PASSPORT"),
    VOTER_CARD(3, "VOTER_CARD"),
    DRIVING_LICENCE(4, "DRIVING_LICENCE"),
    OTHER(7, "OTHER");

    private String value;
    private int key;

    private KycOrAddressDocType(int key, String value) {
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
