package com.punchin.dto;

import com.punchin.enums.CauseOfDeathEnum;
import com.punchin.enums.ClaimStatus;
import lombok.Data;

import java.util.Date;

@Data
public class BankerClaimListResponseDTO {
    private Long id;
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
    private String lenderName;
    private String loanType;
    private String category;
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
    private ClaimStatus claimStatus;
    private ClaimStatus claimBankerStatus;
    private Long agentId;
    private Long bankerId;
    private Long submittedBy;
    private Long submittedAt;
    private Boolean isForwardToVerifier;

    //Field filled by Agent
    private CauseOfDeathEnum causeOfDeath;
    private Boolean isMinor;
    private String agentRemark;
    private String agentComment;
    private String agentName;
    private Boolean agentRemarkNotify = false;
    private Boolean bankerRemarkNotify = false;
    private Boolean agentVerifierRemarkNotify = false;
    private Boolean bankerVerifierRemarkNotify = false;

}
