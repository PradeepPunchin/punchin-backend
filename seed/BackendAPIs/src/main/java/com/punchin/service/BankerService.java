package com.punchin.service;

import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface BankerService {

    Map<String, Object> saveUploadExcelData(MultipartFile[] files);

    Page getAllClaimsData(ClaimStatus claimStatus, Integer page, Integer limit);

    Map<String, Long> getDashboardData();
}
