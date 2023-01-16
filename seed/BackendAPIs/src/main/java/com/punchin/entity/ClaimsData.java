package com.punchin.entity;

import com.punchin.enums.CauseOfDeathEnum;
import com.punchin.enums.ClaimStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimsData extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
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
    @Column(columnDefinition = "Text")
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
    @Column(columnDefinition = "Text")
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
    @Column(columnDefinition = "Text")
    private String nomineeAddress;
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimBankerStatus;
    private Long agentId = 0L;
    private Long bankerId = 0L;
    private Long verifierId = 0L;
    private Long submittedBy;
    private Long submittedAt;
    private Boolean isForwardToVerifier = false;

    //Field filled by Agent
    private CauseOfDeathEnum causeOfDeath;
    private Boolean isMinor;
    private String agentRemark;
    private String agentComment;
    private Boolean agentRemarkNotify = false;
    private Boolean bankerRemarkNotify = false;
    private Boolean agentVerifierRemarkNotify = false;
    private Boolean bankerVerifierRemarkNotify = false;
}
