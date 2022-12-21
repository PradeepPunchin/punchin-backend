package com.punchin.dto;

import com.punchin.entity.ClaimDocuments;
import com.punchin.enums.CauseOfDeathEnum;
import com.punchin.enums.ClaimStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data

public class ClaimDataDTO {

    private Long id;
    private Date uploadDate;
    private String punchinClaimId;

    private String insurerClaimId;

    private String punchinBankerId;

    private Date claimInwardDate;

    private String borrowerName;

    private String borrowerContactNumber;

    private String borrowerEmailId;

    private Date borrowerDob;

    private String loanAccountNumber;

    private String borrowerAddress;

    private String borrowerState;

    private String loanType;

    private Double loanAmount;

    private Double loanAmountPaidByBorrower;

    private Double loanAmountBalance;

    private String branchCode;

    private String branchName;

    private String branchAddress;

    private String branchPinCode;

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

    private String submittedBy;

    private Long submittedAt;

    private Boolean isForwardToVerifier = false;

    //Field filled by Agent
    private CauseOfDeathEnum causeOfDeath;

    private Boolean isMinor;

    private Boolean agentToVerifier;

    private Long agentToVerifierTime;

    private List<ClaimDocuments> claimDocuments;
}
