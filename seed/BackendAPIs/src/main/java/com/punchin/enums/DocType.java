package com.punchin.enums;

public enum DocType {
    CAS(1, "CAS"),
    COVID_Q(2, "COVID_Q"),
    DOGH(3, "DOGH"),
    FUF(4, "FUF"),
    POS_LETTER(5, "POS_LETTER"),
    PROPOSAL_FORM(6, "PROPOSAL_FORM"),
    SANCTION_LETTER(7, "SANCTION_LETTER"),
    SMQ(8, "SMQ"),
    SINGNED_CLAIM_FORM(9, "SINGNED_CLAIM_FORM"),
    DEATH_CERTIFICATE(10, "DEATH_CERTIFICATE"),
    BORROWER_ID_PROOF(11, "BORROWER_ID_PROOF"),
    BORROWER_ADDRESS_PROOF(12, "BORROWER_ADDRESS_PROOF"),
    NOMINEE_ID_PROOF(13, "NOMINEE_ID_PROOF"),
    NOMINEE_ADDRESS_PROOF(14, "NOMINEE_ADDRESS_PROOF"),
    BANK_ACCOUNT_PROOF(15, "BANK_ACCOUNT_PROOF"),
    FIR_POSTMORTEM_REPORT(16, "FIR_POSTMORTEM_REPORT"),
    AFFIDAVIT(17, "AFFIDAVIT"),
    DISCREPANCY(18, "DISCREPANCY");

    private String value;
    private int key;

    private DocType(int key, String value) {
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
