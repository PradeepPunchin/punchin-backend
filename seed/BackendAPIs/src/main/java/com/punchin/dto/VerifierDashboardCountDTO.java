package com.punchin.dto;

import lombok.Data;

@Data
public class VerifierDashboardCountDTO {
    private Long allocatedCount;
    private Long actionPendingCount;
    private Long inProgressCount;
}
