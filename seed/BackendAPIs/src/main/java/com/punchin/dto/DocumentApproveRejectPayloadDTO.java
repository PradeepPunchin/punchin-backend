package com.punchin.dto;

import lombok.Data;

@Data
public class DocumentApproveRejectPayloadDTO {
    private boolean isApproved;
    private String reason;
    private String remark;
}
