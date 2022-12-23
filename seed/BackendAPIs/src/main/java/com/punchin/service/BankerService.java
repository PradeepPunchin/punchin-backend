package com.punchin.service;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.SearchCaseEnum;
import org.springframework.http.ResponseEntity;
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

    ByteArrayInputStream downloadMISFile(ClaimStatus claimStatus);

    String forwardToVerifier(ClaimsData claimsData);

    boolean isBanker();

    ClaimsData isClaimByBanker(Long claimId);

    ClaimDocuments getClaimDocuments(Long docId);

    String deleteBankDocument(ClaimDocuments claimDocuments);

    String saveASDraftDocument(ClaimsData claimsData);

    String downloadMISReport(ClaimDataFilter claimDataFilter);


    ResponseEntity<Object> saveUploadCSVData(MultipartFile file);

    List<Map<String, Object>> getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter);

    PageDTO getBankerClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize);

    boolean checkDocumentAlreadyExist(Long id, BankerDocType docType);
}
