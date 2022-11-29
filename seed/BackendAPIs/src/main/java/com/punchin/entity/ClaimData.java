package com.punchin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "claim_data")
public class ClaimData extends BasicEntity {

    @Id
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
