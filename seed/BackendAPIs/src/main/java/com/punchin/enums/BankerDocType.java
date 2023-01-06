package com.punchin.enums;

public enum BankerDocType {
    CAS(1, "CAS"),
    COVID_Q(2, "COVID_Q"),
    DOGH(3, "DOGH"),
    FUF(4, "FUF"),
    POS_LETTER(5, "POS_LETTER"),
    PROPOSAL_FORM(6, "PROPOSAL_FORM"),
    SANCTION_LETTER(7, "SANCTION_LETTER"),
    SMQ(8, "SMQ"),
    MEF(9, "MEF"),
    OTHER(10, "OTHER");

    private String value;
    private int key;

    private BankerDocType(int key, String value) {
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
