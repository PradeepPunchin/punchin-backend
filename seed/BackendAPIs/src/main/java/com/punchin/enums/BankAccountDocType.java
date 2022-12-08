package com.punchin.enums;

public enum BankAccountDocType {
    BANK_PASSBOOK(1, "BANK_PASSBOOK"),
    BANK_STATEMENT(2, "BANK_STATEMENT"),
    CHEQUE_LEAF(3, "CHEQUE_LEAF"),
    NEFT_FORM(4, "NEFT_FORM"),
    OTHER(5, "OTHER");

    private String value;
    private int key;

    private BankAccountDocType(int key, String value) {
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
