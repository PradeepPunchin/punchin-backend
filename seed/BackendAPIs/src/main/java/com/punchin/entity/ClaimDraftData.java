package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimDraftData extends BasicEntity {

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
    private String loanAccountNumber;
    @Column(columnDefinition = "Text")
    private String borrowerAddress;
    private String loanType;
    private String category;
    private Date loanDisbursalDate;
    private Double loanOutstandingAmount;
    private Double loanAmount;
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
}
