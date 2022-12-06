package com.punchin.service;

import com.punchin.dto.PageDTO;
import org.springframework.stereotype.Service;

@Service
public class CommonUtilService {


    public PageDTO getDetailsPage(Object contentList, long size, Integer totalPages, long totalrecords) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setContent(contentList);
        pageDTO.setSize(size);
        pageDTO.setTotalPages(totalPages);
        pageDTO.setTotalRecords(totalrecords);
        return pageDTO;
    }
}
