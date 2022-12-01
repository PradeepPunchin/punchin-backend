package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimDocuments extends BasicEntity{

    @ManyToOne
    private ClaimsData claimsData;

    private String claimType;

    private String documentUrl;

    private Long uploadTime;

    private String agentId;

    private Boolean isVerified;

    private Boolean isApproved;

    private String verifierId;

    private Long verifyTime;

}
