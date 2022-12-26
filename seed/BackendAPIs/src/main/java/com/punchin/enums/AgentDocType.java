package com.punchin.enums;

public enum AgentDocType {
    SIGNED_FORM(1, "SIGNED_FORM"),
    DEATH_CERTIFICATE(2, "DEATH_CERTIFICATE"),
    BORROWER_ID_PROOF(3, "BORROWER_ID_PROOF"),
    BORROWER_ADDRESS_PROOF(4, "BORROWER_ADDRESS_PROOF"),
    NOMINEE_ID_PROOF(3, "NOMINEE_ID_PROOF"),
    NOMINEE_ADDRESS_PROOF(5, "NOMINEE_ADDRESS_PROOF"),
    BANK_ACCOUNT_PROOF(6, "BANK_ACCOUNT_PROOF"),
    FIR_POSTMORTEM_REPORT(7, "FIR_POSTMORTEM_REPORT"),
    ADDITIONAL(8, "ADDITIONAL"),
    BANK_PASSBOOK(9, "BANK_PASSBOOK"),
    BANK_STATEMENT(10, "BANK_STATEMENT"),
    CHEQUE_LEAF(11, "CHEQUE_LEAF"),
    NEFT_FORM(12, "NEFT_FORM"),
    INCOME_TAX_RETURN(13, "INCOME_TAX_RETURN"),
    MEDICAL_RECORDS(14, "MEDICAL_RECORDS"),
    LEGAL_HEIR_CERTIFICATE(15, "LEGAL_HEIR_CERTIFICATE"),
    POLICE_INVESTIGATION_REPORT(16, "POLICE_INVESTIGATION_REPORT"),
    OTHER(17, "OTHER"),
    CAS(18, "CAS"),
    COVID_Q(19, "COVID_Q"),
    DOGH(20, "DOGH"),
    FUF(21, "FUF"),
    POS_LETTER(22, "POS_LETTER"),
    PROPOSAL_FORM(23, "PROPOSAL_FORM"),
    SANCTION_LETTER(24, "SANCTION_LETTER"),
    SMQ(25, "SMQ"),
    RELATIONSHIP_PROOF(1, "RELATIONSHIP_PROOF"),
    GUARDIAN_ID_PROOF(2, "GUARDIAN_ID_PROOF"),
    GUARDIAN_ADD_PROOF(3, "GUARDIAN_ADD_PROOF"),
    MEF(26, "MEF");

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
