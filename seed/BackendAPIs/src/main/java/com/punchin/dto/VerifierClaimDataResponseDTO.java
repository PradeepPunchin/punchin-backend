package com.punchin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VerifierClaimDataResponseDTO {

    private Long id;
    private LocalDate registrationDate;
    private String borrowerName;
    private String nomineeName;
    private String nomineeContactNumber;
    private String nomineeAddress;
    private boolean singnedClaimDocument;
    private boolean deathCertificate;
    private boolean borrowerIdProof;
    private boolean borrowerAddressProof;
    private boolean nomineeIdProof;
    private boolean nomineeAddressProof;
    private boolean bankAccountProof;
    private boolean FIRPostmortemReport;
    private boolean affidavit;
    private boolean dicrepancy;
}


