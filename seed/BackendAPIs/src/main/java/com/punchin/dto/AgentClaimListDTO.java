package com.punchin.dto;

import com.punchin.enums.ClaimStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AgentClaimListDTO {

    private Long id;
    private String claimId;
    private Date claimDate;
    private String nomineeName;
    private String nomineeContactNumber;
    private Date allocationDate;
    private String borrowerName;
    private String borrowerAddress;

    private ClaimStatus claimStatus;
    private List<ClaimsRemarksDTO> claimsRemarksDTOs;
}
