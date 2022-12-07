package com.punchin.dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentDetailsDTO {

    private long documentId;

    private String documentName;

    private boolean documentUploaded;

    private String documentStatus;

    private List<DocumentUrlListDTO> documentUrlListDTOList;

}
