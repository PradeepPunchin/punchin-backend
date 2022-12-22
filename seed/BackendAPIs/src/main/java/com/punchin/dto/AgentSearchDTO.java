package com.punchin.dto;

import com.punchin.enums.ClaimStatus;
import lombok.Data;

import java.util.Date;

@Data
public class AgentSearchDTO {

    private String claimId;
    private Date claimDate;
    private Date allocationDate;
    private ClaimStatus claimStatus;
    private Date uploadDate;
    private String punchinClaimId;
    private String insurerClaimId;
    private String punchinBankerId;
    private Date claimInwardDate;
    private String borrowerName;
    private String borrowerContactNumber;
    private String borrowerCity;
    private String borrowerState;
    private String borrowerPinCode;
    private String borrowerEmailId;
    private String borrowerAlternateContactNumber;
    private String borrowerAlternateContactDetails;
    private Date borrowerDob;
    private String loanAccountNumber;
    private String borrowerAddress;
    private String loanType;
    private Date loanDisbursalDate;
    private Double loanOutstandingAmount;
    private Double loanAmount;
    private Double loanAmountPaidByBorrower;
    private Double loanAmountBalance;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String branchPinCode;
    private String branchCity;
    private String branchState;
    private String loanAccountManagerName;
    private String accountManagerContactNumber;
    private String insurerName;
    private String masterPolNumber;
    private String policyNumber;
    private Date policyStartDate;
    private Integer policyCoverageDuration;
    private Double policySumAssured;
    private String nomineeName;
    private String nomineeRelationShip;
    private String nomineeContactNumber;
    private String nomineeEmailId;
    private String nomineeAddress;


}

