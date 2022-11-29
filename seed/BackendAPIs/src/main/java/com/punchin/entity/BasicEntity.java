package com.punchin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import java.io.Serializable;
import java.util.Date;

@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"updatedAt"},
        allowGetters = true
)
public class BasicEntity implements Serializable {

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
