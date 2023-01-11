package com.punchin.enums;

public enum AgentDocType {
    SIGNED_FORM(1, "SIGNED_FORM"),
    DEATH_CERTIFICATE(2, "DEATH_CERTIFICATE"),
    BANK_ACCOUNT_PROOF(3, "BANK_ACCOUNT_PROOF"),
    FIR_REPORT(4, "FIR_REPORT"),
    POSTMORTEM_REPORT(5, "POSTMORTEM_REPORT"),
    ADDITIONAL(6, "ADDITIONAL"),
    BANK_PASSBOOK(7, "BANK_PASSBOOK"),
    BANK_STATEMENT(8, "BANK_STATEMENT"),
    CHEQUE_LEAF(9, "CHEQUE_LEAF"),
    NEFT_FORM(10, "NEFT_FORM"),
    INCOME_TAX_RETURN(11, "INCOME_TAX_RETURN"),
    MEDICAL_RECORDS(12, "MEDICAL_RECORDS"),
    LEGAL_HEIR_CERTIFICATE(13, "LEGAL_HEIR_CERTIFICATE"),
    POLICE_INVESTIGATION_REPORT(14, "POLICE_INVESTIGATION_REPORT"),
    OTHER(15, "OTHER"),
    CAS(16, "CAS"),
    COVID_Q(17, "COVID_Q"),
    DOGH(18, "DOGH"),
    FUF(19, "FUF"),
    POS_LETTER(20, "POS_LETTER"),
    PROPOSAL_FORM(21, "PROPOSAL_FORM"),
    SANCTION_LETTER(22, "SANCTION_LETTER"),
    SMQ(23, "SMQ"),
    RELATIONSHIP_PROOF(24, "RELATIONSHIP_PROOF"),
    GUARDIAN_ID_PROOF(25, "GUARDIAN_ID_PROOF"),
    GUARDIAN_ADD_PROOF(26, "GUARDIAN_ADD_PROOF"),
    MEF(27, "MEF"),
    BORROWER_KYC_PROOF(28, "BORROWER_KYC_PROOF"),
    NOMINEE_KYC_PROOF(29, "NOMINEE_KYC_PROOF"),
    STAMPED_AFFIDAVIT(30, "STAMPED_AFFIDAVIT"),
    ANY_UTILITY_BILL(31, "ANY_UTILITY_BILL"),
    MEDICAL_ATTENDANT_CERTIFICATE(32, "MEDICAL_ATTENDANT_CERTIFICATE"),
    OTHER_MINOR(32, "OTHER_MINOR");

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
