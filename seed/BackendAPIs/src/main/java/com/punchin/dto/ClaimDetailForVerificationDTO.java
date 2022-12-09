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

    private List<ClaimDocumentsDTO> claimDocumentsDTOS;


}
