package com.punchin.enums;

import lombok.Data;

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
}
