package com.punchin.enums;

public enum AgentDocType {
    SIGNED_FORM(1, "SIGNED_FORM"),
    DEATH_CERTIFICATE(2, "DEATH_CERTIFICATE"),
    BORROWER_ID_PROOF(3, "BORROWER_ID_PROOF"),
    BORROWER_ADDRESS_PROOF(4, "BORROWER_ADDRESS_PROOF"),
    NOMINEE_ID_PROOF(3, "NOMINEE_ID_PROOF"),
    NOMINEE_ADDRESS_PROOF(4, "NOMINEE_ADDRESS_PROOF"),
    BANK_ACCOUNT_PROOF(4, "BANK_ACCOUNT_PROOF"),
    FIR_POSTMORTEM_REPORT(4, "FIR_POSTMORTEM_REPORT"),
    ADDITIONAL(4, "ADDITIONAL");

    private String value;
    private int key;

    private AgentDocType(int key, String value) {
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
