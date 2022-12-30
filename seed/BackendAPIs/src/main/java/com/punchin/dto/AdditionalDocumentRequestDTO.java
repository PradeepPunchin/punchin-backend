package com.punchin.dto;

import com.punchin.enums.AgentDocType;
import lombok.Data;

import java.util.List;

@Data
public class AdditionalDocumentRequestDTO {
    private Long claimId;
    private List<AgentDocType> docTypes;
    private String remark;
}
