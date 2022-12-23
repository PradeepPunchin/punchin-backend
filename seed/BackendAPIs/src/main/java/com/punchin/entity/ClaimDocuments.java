package com.punchin.entity;

import com.punchin.enums.AgentDocType;
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
    @ManyToOne(fetch = FetchType.EAGER)
    private ClaimsData claimsData;
    @Enumerated(EnumType.STRING)
    private AgentDocType agentDocType;
    private String docType;
    @OneToMany
    private List<DocumentUrls> documentUrls;
    private String uploadSideBy;
    private String uploadBy;
    private Long uploadTime;
    private Boolean isVerified = false;
    private Boolean isApproved = false;
    private String verifierId;
    private Long verifyTime;
    private String reason;
    @Column(columnDefinition = "Text")
    private String rejectRemark;

}
