package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimUploadDraft extends BasicEntity {

    private String punchinClaimId;

    private String insurerClaimId;

    private String punchinBankerId;

    private LocalDate claimInwardDate;

    private String borrowerName;

    private String borrowerContactNumber;

    private String loanAccountNumber;

    @Column(columnDefinition = "Text")
    private String borrowerAddress;

    private String loanType;

    private Double loanAmount;

    private String branchCode;

    private String branchName;

    @Column(columnDefinition = "Text")
    private String branchAddress;

    private String branchPinCode;

    private String branchState;

    private String loanAccountManagerName;

    private String accountManagerContactNumber;

    private String insurerName;

    private String masterPolNumber;

    private String policyNumber;

    private LocalDate policyStartDate;

    private Integer policyCoverageDuration;

    private Double policySumAssured;

    private String nomineeName;

    private String nomineeRelationShip;

    private String nomineeContactNumber;

    private String nomineeEmailId;

    @Column(columnDefinition = "Text")
    private String nomineeAddress;
}
