package com.punchin.service;

import com.punchin.dto.PageDTO;
import org.springframework.data.domain.Page;
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

    public PageDTO convertPageToDTO(Page page) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setContent(page.getContent());
        pageDTO.setSize(page.getSize());
        pageDTO.setTotalPages(page.getTotalPages());
        pageDTO.setTotalRecords(page.getNumberOfElements());
        return pageDTO;
    }

    public PageDTO convertPageToDTO(Object content, Page page) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setContent(content);
        pageDTO.setSize(page.getSize());
        pageDTO.setTotalPages(page.getTotalPages());
        pageDTO.setTotalRecords(page.getNumberOfElements());
        return pageDTO;
    }
}
