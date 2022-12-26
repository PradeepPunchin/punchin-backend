package com.punchin.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadResponseUrl {

    private String docType;
    private List<String> urls;
}
