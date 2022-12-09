package com.punchin.dto;

import lombok.Data;

import java.util.Date;

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
}
