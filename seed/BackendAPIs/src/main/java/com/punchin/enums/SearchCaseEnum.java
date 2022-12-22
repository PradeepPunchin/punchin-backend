package com.punchin.enums;

public enum SearchCaseEnum {
    CLAIM_DATA_ID(1, "Claim Id"), LOAN_ACCOUNT_NUMBER(2, "Loan Account Number"), NAME(3, "Name");
    private String value;
    private int key;

    private SearchCaseEnum(int key, String value) {
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
