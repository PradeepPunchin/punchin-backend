package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class DocumentUrls extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

    private String docUrl;
    private ClaimDocuments claimDocuments;
}
