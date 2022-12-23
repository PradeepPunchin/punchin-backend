package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.SearchCaseEnum;

import java.util.List;
import java.util.Map;

public interface VerifierService {

    PageDTO getAllClaimsData(ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize);

    Map<String, Long> getDashboardData();

    ClaimsData getClaimData(Long claimId);

    boolean allocateClaimToAgent(ClaimsData claimsData, User user);

    ClaimDetailForVerificationDTO getClaimDocuments(ClaimsData claimsData);

    String acceptAndRejectDocument(ClaimsData claimsData, ClaimDocuments claimDocuments, DocumentApproveRejectPayloadDTO approveRejectPayloadDTO);

    ClaimDocuments getClaimDocumentById(Long docId);

    PageDTO getClaimDataWithDocumentStatus(Integer page, Integer limit);

    String downloadAllDocuments(Long claimId);

    String downloadClaimDataWithDocumentStatus(Integer page, Integer limit);

    PageDTO getVerifierClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, ClaimDataFilter claimDataFilter, Integer pageNo, Integer limit);

    List<AgentListResponseDTO> getAllAgentsForVerifier(long userId);

    String downloadMISReport(ClaimDataFilter claimDataFilter);
}
