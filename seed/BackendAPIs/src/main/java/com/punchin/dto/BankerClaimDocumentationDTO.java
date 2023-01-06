package com.punchin.dto;

import com.punchin.entity.ClaimDocuments;
import lombok.Data;

import java.util.List;

@Data
public class BankerClaimDocumentationDTO {

    private Long id;
    private String punchinClaimId;
    private String borrowerName;
    private String borrowerAddress;
    private String loanType;
    private String loanAccountNumber;
    private String insurerName;
    private String masterPolicyNumbet;
    private String borrowerPolicyNumber;
    private Double policySumAssured = 0D;
    private Double loanAmount = 0D;
    private Double loanAmountPaidByBorrower = 0D;
    private Double outstandingLoanAmount = 0D;
    private Double balanceClaimAmount = 0D;
    private boolean isSubmitted = false;
    private List<ClaimDocumentsDTO> claimDocumentsDTOS;
}
