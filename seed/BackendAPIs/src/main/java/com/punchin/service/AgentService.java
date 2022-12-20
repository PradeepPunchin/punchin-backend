package com.punchin.service;

import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AgentService {
    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    Map<String, Object> getDashboardData();

    boolean checkAccess(Long claimId);

    ClaimsData getClaimData(Long claimId);

    Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO);

    Map<String, Object> getClaimDocuments(Long id);

    Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] files, String docType);

    boolean checkDocumentIsInDiscrepancy(Long claimId, String docType);

    boolean checkDocumentUploaded(Long id);

    String forwardToVerifier(Long id);

    PageDTO getClaimSearchedData(String caseType, String searchedKeyword, Integer pageNo, Integer limit);
}
