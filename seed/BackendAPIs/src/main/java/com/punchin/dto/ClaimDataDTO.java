package com.punchin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data

public class ClaimDataDTO {

    private Integer serialNumber;

    private Integer punchinclaimId;

    private String insurerClaimId;

    private LocalDateTime claimInwardDate;

    private String borrowerName;

    private Integer borrowerContactNumber;

    private Long loanAccountNumber;

    private String borrowerAddress;

    private String loanType;

    private Long loanAmount;

    private Integer branchCode;

    private String branchName;

    private String branchAddress;

    private Integer branchPinCode;

    private String state;

    private String loanAmountMgrName;

    private Long acntMgrPhoneNumber;

    private String insurerName;

    private Integer borrowerPolicyNumber;

    private String masterPolNumber;

    private LocalDateTime policyStartdate;

    private Integer policyCoverageDuration;

    private Double policySumAssured;

    private String nomineeName;

    private String nomineeRelationShip;

    private Long nomineeContactNumber;

    private String nomineeEmailId;

    private String nomineeAddress;

    private String statusEnum;
}
