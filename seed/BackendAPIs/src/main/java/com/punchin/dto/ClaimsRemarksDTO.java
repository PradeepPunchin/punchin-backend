package com.punchin.dto;

import lombok.Data;

@Data
public class ClaimsRemarksDTO {
    private Long claimId;
    private Long remarkDoneBy;
    private String remark;
    private String role;
}
