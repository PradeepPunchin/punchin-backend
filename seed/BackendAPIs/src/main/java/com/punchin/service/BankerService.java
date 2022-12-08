package com.punchin.service;

import com.punchin.entity.ClaimsData;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BankerService {

    Map<String, Object> saveUploadExcelData(MultipartFile[] files);

    Page getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    Map<String, Long> getDashboardData();

    String submitClaims();

    String discardClaims();

    ClaimsData getClaimData(Long claimId);

    Map<String, Object> uploadDocument(ClaimsData claimsData, MultipartFile[] multipartFiles, BankerDocType docType);
    List<ClaimsData> downloadMISFile(ClaimStatus claimStatus);

    String forwardToVerifier(ClaimsData claimsData);
}
