package com.punchin.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClaimDocumentsDTO {
    private Long id;
    private String docType;
    private Boolean isVerified = false;
    private Boolean isApproved = false;
    private List<DocumentUrlDTO> documentUrlDTOS;

}
