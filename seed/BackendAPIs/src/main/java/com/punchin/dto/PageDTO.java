package com.punchin.dto;

import lombok.Data;

@Data
public class PageDTO {

    private Object content;

    private long size;

    private int totalPages;

    private long totalRecords;
}
