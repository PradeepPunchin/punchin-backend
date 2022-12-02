package com.punchin.entity;

import com.punchin.enums.ClaimStatus;
import com.punchin.enums.Platform;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimsData extends BasicEntity {

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

    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    private String submittedBy;

    private Long submittedAt;

    @OneToMany
    private List<Platform.ClaimDocuments> claimDocuments;



}
