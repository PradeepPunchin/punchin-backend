package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class BankerVerifierRemark extends BasicEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;
    private Long claimId;
    private Long remarkDoneBy;
    private String remark;
    private String role;

    public BankerVerifierRemark(Long claimId, String remark, Long remarkDoneBy, String role) {
        this.claimId = claimId;
        this.remark = remark;
        this.remarkDoneBy = remarkDoneBy;
        this.role = role;
    }

    public BankerVerifierRemark() {}
}
