package com.punchin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class DocumentTypes extends BasicEntity {

    private String documentType;
    private String documentName;

    @Column(columnDefinition = "Text")
    private String documentDescription;
}
