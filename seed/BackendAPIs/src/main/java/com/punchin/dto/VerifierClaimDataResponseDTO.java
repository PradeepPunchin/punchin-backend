package com.punchin.dto;

import lombok.Data;

import java.util.Date;

@Data
public class VerifierClaimDataResponseDTO {

    private Long id;
    private Date claimDate;
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
    private Boolean firPostmortemReport;
}


