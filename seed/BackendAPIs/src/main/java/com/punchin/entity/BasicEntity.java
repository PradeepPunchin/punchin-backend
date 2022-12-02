package com.punchin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"updatedAt"},
        allowGetters = true
)
public class BasicEntity implements Serializable {

    @Column
    private Long createdAt;

    @Column
    private Long updatedAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    public boolean getIsDeleted() {
        if (isDeleted != null){
            return isDeleted;
        }
        else{
            return false;
        }
    }

    public void setIsDeleted(Boolean isDeleted) {
        if (isDeleted != null) this.isDeleted = isDeleted;
    }

    public Long getCreatedDate() {
        return createdAt;
    }

    @PrePersist
    public void setCreatedDate() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean getIsActive() {
        if (isActive != null){
            return isActive;
        }
        else {
            return true;
        }
    }

    public void setIsActive(Boolean isActive) {
        if (isActive != null) this.isActive = isActive;
    }
}
