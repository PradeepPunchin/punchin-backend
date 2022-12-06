

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
    private String singnedClaimDocument;
    private String deathCertificate;
    private String borrowerIdProof;
    private String borrowerAddressProof;
    private String nomineeIdProof;
    private String nomineeAddressProof;
    private String bankAccountProof;
    private String FIRPostmortemReport;
    private String affidavit;
    private String dicrepancy;
}


