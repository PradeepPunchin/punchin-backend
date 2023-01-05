package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClaimsRemarks extends BasicEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;
    private Long claimId;
    private Long agentId;
    private String remark;
    private String comment;

    public ClaimsRemarks(Long claimId, String remark, String comment, Long agentId) {
        this.claimId = claimId;
        this.remark = remark;
        this.comment = comment;
        this.agentId = agentId;
    }

    public ClaimsRemarks() {}
}
