package com.punchin.dto;

import com.punchin.enums.ClaimStatus;
import lombok.Data;

@Data
public class ClaimHistoryDTO {
    private Long claimId;
    private ClaimStatus claimStatus;
    private String description;
}
