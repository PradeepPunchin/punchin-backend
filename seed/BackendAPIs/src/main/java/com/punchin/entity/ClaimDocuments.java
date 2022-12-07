package com.punchin.entity;

import com.punchin.enums.ClaimDocumentsStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimDocuments extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

    private ClaimsData claimsData;

    private String docType;

    @OneToMany
    private List<DocumentUrls> documentUrls;

    private String bankerId;

    private String agentId;

    private Long uploadTime;

    private Boolean isVerified = false;

    private Boolean isApproved = false;

    private String verifierId;

    private Long verifyTime;

    @Enumerated(EnumType.STRING)
    private ClaimDocumentsStatus claimDocumentsStatus;

}
