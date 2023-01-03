package com.punchin.entity;

import com.punchin.enums.ClaimStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimHistory extends BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;
    private Long claimId;
    private ClaimStatus claimStatus;
    private String description;

    public ClaimHistory(Long claimId, ClaimStatus claimStatus, String description) {
        this.claimId = claimId;
        this.claimStatus = claimStatus;
        this.description = description;
    }

    public ClaimHistory() {}
}
