package com.punchin.service;

import com.punchin.dto.AgentSearchDTO;
import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.AgentDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.SearchCaseEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    List<AgentSearchDTO> getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter);

    List<DocumentUrls> uploadAgentDocument(Long id, MultipartFile[] multipartFiles, AgentDocType docType);

    String deleteClaimDocument(Long documentId);
}
