package com.punchin.dto;

import lombok.Data;

@Data
public class AgentDocumentStatusDTO {
    private String agentDocName;

    private String status = "NOT UPLOADED";
}
