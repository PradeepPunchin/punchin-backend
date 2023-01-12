package com.punchin.dto;

import com.punchin.enums.ClaimStatus;
import lombok.Data;

import java.util.Date;

@Data
public class DownloadVerifierMisResponse {

    private String punchinClaimId;
    private Date claimInwardDate;
    private String borrowerName;
    private String borrowerContactNumber;
    private String borrowerCity;
    private String borrowerState;
    private String borrowerPinCode;
    private String borrowerAlternateContactNumber;
    private String borrowerAddress;
    private String insurerName;
    private String nomineeName;
    private String nomineeRelationShip;
    private String nomineeContactNumber;
    private String nomineeAddress;
    private ClaimStatus claimStatus;
    private Long agentId = 0L;
    private String agentMapped;
    private String agentName;
}
