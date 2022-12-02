package com.punchin.enums;

import com.punchin.entity.BasicEntity;
import com.punchin.entity.ClaimsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

public enum Platform {
    WEB(1, "web"),ANDROID(2, "android"),IOS(3, "ios");

    private String value;
    private int key;

    private Platform(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ClaimDocuments extends BasicEntity {

        @ManyToOne
        private ClaimsData claimsData;

        private String claimType;

        private String documentUrl;

        private Long uploadTime;

        private String bankerId;

        private String agentId;

        private Boolean isVerified;

        private Boolean isApproved;

        private String verifierId;

        private Long verifyTime;

        public static class ClaimsDraft {
        }
    }
}
