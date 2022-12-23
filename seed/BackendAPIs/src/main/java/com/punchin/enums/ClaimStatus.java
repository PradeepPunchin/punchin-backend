package com.punchin.enums;

public enum ClaimStatus {

    ALL(0, "ALL"),//All
    CLAIM_INTIMATED(11, "CLAIM_INTIMATED"),//When Banker upload claim data
    BANKER_DISCREPANCY(12, "BANKER_DISCREPANCY"),//When Banker upload claim data
    CLAIM_SUBMITTED(1, "CLAIM_SUBMITTED"),//When Banker upload claim document and submit
    AGENT_ALLOCATED(2, "AGENT_ALLOCATED"),//When claim is allocated to Agent
    IN_PROGRESS(3, "IN_PROGRESS"),//When Agent to some changes to claim
    ACTION_PENDING(4, "ACTION_PENDING"),//"Agent Allocated -  Inprogress = Action Pending means no action is taken by Agent till now on that claim"
    UNDER_VERIFICATION(5, "UNDER_VERIFICATION"),//When it comes to Verifier to verify document uploaded by Agent
    VERIFIER_DISCREPENCY(6, "VERIFIER_DISCREPENCY"),//When verifier reject any document or other info
    SUBMITTED_TO_INSURER(7, "SUBMITTED_TO_INSURER"),//When claim info is submitted to insurer via API
    INSURER_DISCREPENCY(8, "INSURER_DISCREPENCY"),//When insurer reject any document or other info in claim case
    INSURER_REJECTED(9, "INSURER_REJECTED"),//Claim is rejected/not accepted by Insurer due to some reason
    SETTLED(10, "SETTLED");//Claim is Settled or paid by Insurer


    private final String value;

    private final int key;

    private ClaimStatus(int key, String value) {
        this.key = key;
        this.value = value;
    }

}
