package com.punchin.service;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

public interface BankerService {

    Map<String, Object> saveUploadExcelData(MultipartFile[] files);

    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    Map<String, Long> getDashboardData();

    String submitClaims();

    String discardClaims();

    BankerClaimDocumentationDTO getClaimDataForBankerAction(Long claimId);

    ClaimsData getClaimData(Long claimId);

    Map<String, Object> uploadDocument(ClaimsData claimsData, MultipartFile[] multipartFiles, BankerDocType docType);

    ByteArrayInputStream downloadMISFile();

    String forwardToVerifier(ClaimsData claimsData);

    boolean isBanker();

    ClaimsData isClaimByBanker(Long claimId);

    ClaimDocuments getClaimDocuments(Long docId);

    String deleteBankDocument(ClaimDocuments claimDocuments);

    String saveASDraftDocument(ClaimsData claimsData);

    String downloadMISReport(ClaimDataFilter claimDataFilter);
}
