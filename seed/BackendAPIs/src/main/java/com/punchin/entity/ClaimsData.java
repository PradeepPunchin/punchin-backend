package com.punchin.entity;

import com.punchin.enums.CauseOfDeathEnum;
import com.punchin.enums.ClaimStatus;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimsData extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;
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
    private String loanType;
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
    private String submittedBy;
    private Long submittedAt;
    private Boolean isForwardToVerifier = false;
    private Long bankerToVerifierTime;
    //Field filled by Agent
    private CauseOfDeathEnum causeOfDeath;
    private Boolean isMinor;

    private Boolean agentToVerifier;

    private Long agentToVerifierTime;

}
