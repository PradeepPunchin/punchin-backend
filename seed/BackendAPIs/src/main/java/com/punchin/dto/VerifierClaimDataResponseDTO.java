

package com.punchin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VerifierClaimDataResponseDTO {

    private Long id;
    private LocalDate claimInwardDate;
    private String borrowerName;
    private String nomineeName;
    private Long nomineeContactNumber;
    private String nomineeAddress;
    private String singnedClaimDocument;
    private String deathCertificate;

}


