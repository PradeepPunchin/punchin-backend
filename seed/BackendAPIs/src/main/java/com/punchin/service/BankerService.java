package com.punchin.service;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.dto.ClaimHistoryDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.AgentDocType;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.SearchCaseEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BankerService {

    Map<String, Object> saveUploadExcelData(MultipartFile[] files) throws IOException;

    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit, String searchedKeyword, SearchCaseEnum searchCaseEnum);

    Map<String, Long> getDashboardData();

    String submitClaims();

    String discardClaims();

    BankerClaimDocumentationDTO getClaimDataForBankerAction(Long claimId);

    ClaimsData getClaimData(Long claimId);

    Map<String, Object> uploadDocument(ClaimsData claimsData, MultipartFile[] multipartFiles, BankerDocType docType);

    String forwardToVerifier(ClaimsData claimsData);

    boolean isBanker();

    ClaimsData isClaimByBanker(Long claimId);

    ClaimDocuments getClaimDocuments(Long docId);

    String deleteBankDocument(ClaimDocuments claimDocuments);

    String saveASDraftDocument(ClaimsData claimsData);

    ResponseEntity<Object> saveUploadCSVData(MultipartFile file);

    List<Map<String, Object>> getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter);

    PageDTO getBankerClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize);

    boolean checkDocumentAlreadyExist(Long id, BankerDocType docType);

    Map<String, Object> getClaimBankerDocuments(Long id);

    boolean checkDocumentIsInDiscrepancy(Long id, String docType);

    Map<String, Object> discrepancyDocumentUpload(Long id, MultipartFile[] files, String docType);

    boolean requestForAdditionalDocument(ClaimsData claimsData, List<AgentDocType> docTypes, String remark);

    String downloadAllDocuments(Long id);

    List<ClaimHistoryDTO> getClaimHistory(Long id);
}
