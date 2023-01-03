package com.punchin.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClaimDetailForVerificationDTO {
    private String borrowerName;
    private String loanAccountNumber;
    private String borrowerAddress;
    private String insurerName;
    private String nomineeName;
    private String nomineeAddress;
    private String nomineeRelationShip;
    private List<ClaimDocumentsDTO> agentClaimDocumentsDTOs;
    private List<ClaimDocumentsDTO> bankerClaimDocumentsDTOs;
<<<<<<< HEAD
    private List<ClaimDocumentsDTO> newDocumentRequestDTOs;
=======
    private String claimStatus;

>>>>>>> 1733218aaa058d78fa710e4b737c03cb303025ec

}
