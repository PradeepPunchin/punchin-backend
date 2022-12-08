package com.punchin.dto;

import lombok.Data;

@Data
public class VerifierClaimDataResponseDTO {

    private Long id;
    private Object registrationDate;
    private String borrowerName;
    private String nomineeName;
    private String nomineeContactNumber;
    private String nomineeAddress;
    private Boolean singnedClaimDocument;
    private Boolean deathCertificate;
    private Boolean borrowerIdProof;
    private Boolean borrowerAddressProof;
    private Boolean nomineeIdProof;
    private Boolean nomineeAddressProof;
    private Boolean bankAccountProof;
    private Boolean FIRPostmortemReport;
    private Boolean affidavit;
    private Boolean dicrepancy;
}


