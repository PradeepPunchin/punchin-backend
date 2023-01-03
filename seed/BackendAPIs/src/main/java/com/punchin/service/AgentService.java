package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AgentService {
    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    Map<String, Object> getDashboardData();

    boolean checkAccess(Long claimId);

    Map<String, Object> getClaimData(Long claimId);
    ClaimsData getClaimsData(Long claimId);

    Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO);

    Map<String, Object> getClaimDocuments(Long id);

    Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] files, AgentDocType docType, boolean isDiscrepancy);

    boolean checkDocumentIsInDiscrepancy(Long claimId, AgentDocType docType, boolean isDiscrepancy);

    boolean checkDocumentUploaded(Long id);

    String forwardToVerifier(Long id);

    PageDTO getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter);

    String deleteClaimDocument(Long documentId);

    List<DocumentUrls> uploadAgentDocument(Long id, MultipartFile[] multipartFiles, AgentDocType docType);


    List<ClaimHistoryDTO> getClaimHistory(String id);
}
