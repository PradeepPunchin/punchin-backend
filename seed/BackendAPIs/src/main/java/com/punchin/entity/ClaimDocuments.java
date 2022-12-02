package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimDocuments extends BasicEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

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
