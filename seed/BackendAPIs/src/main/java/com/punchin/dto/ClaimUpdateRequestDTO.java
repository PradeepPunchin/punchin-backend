package com.punchin.dto;

import lombok.Data;

@Data
public class ClaimUpdateRequestDTO {
    private String borrowerName;
    private String borrowerAddress;
    private String borrowerContactNumber;
    private String nomineeName;
    private String nomineeAddress;
    private String nomineeContactNumber;
    private String borrowerPinCode;
}
