package com.punchin.enums;

public enum AdditionalDocType {
    INCOME_TAX_RETURN(1, "INCOME_TAX_RETURN"),
    MEDICAL_RECORDS(2, "MEDICAL_RECORDS"),
    LEGAL_HEIR_CERTIFICATE(3, "LEGAL_HEIR_CERTIFICATE"),
    POLICE_INVESTIGATION_REPORT(4, "POLICE_INVESTIGATION_REPORT"),
    OTHER(5, "OTHER");

    private String value;
    private int key;

    private AdditionalDocType(int key, String value) {
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
