package com.punchin.dto;

import com.punchin.enums.ClaimStatus;
import lombok.Data;

import java.util.Date;

@Data
public class VerifierClaimDataResponseDTO {

    private Long id;
    private String punchinClaimId;
    private Date claimDate;
    private String borrowerName;
    private String borrowerContactNumber;
    private String nomineeName;
    private String nomineeContactNumber;
    private String nomineeAddress;
    private String borrowerAddress;
    private ClaimStatus claimStatus;
    private ClaimStatus lenderName;
    private String singnedClaimDocument = "NOT_UPLOADED";
    private String deathCertificate = "NOT_UPLOADED";
    private String bankAccountProof = "NOT_UPLOADED";
    private String additionalDoc = "NOT_UPLOADED";
    private String relationshipDoc = "NOT_UPLOADED";
    private String guardianIdProof = "NOT_UPLOADED";
    private String guardianAddressProof = "NOT_UPLOADED";
    private String nomineeKycProof = "NOT_UPLOADED";
    private String borrowerKycProof = "NOT_UPLOADED";
    private boolean isAgentAllocated = false;
    private String agentName;
    private String agentCity;
    private String agentState;
}


