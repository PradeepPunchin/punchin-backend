package com.punchin.dto;

import com.punchin.enums.AgentDocType;
import lombok.Data;

import java.util.List;

@Data
public class ClaimDocumentsDTO {
    private Long id;
    private AgentDocType agentDocType;
    private String docType;
    private Boolean isVerified = false;
    private Boolean isApproved = false;
    private String reason;
    private List<DocumentUrlDTO> documentUrlDTOS;

    private List<AgentDocumentStatusDTO> agentDocumentStatus;

}
