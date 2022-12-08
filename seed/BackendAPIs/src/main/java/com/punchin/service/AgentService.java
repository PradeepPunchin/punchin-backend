package com.punchin.service;

import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;

import java.util.List;
import java.util.Map;

public interface AgentService {
    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    List<ClaimsData> getClaimsByAgentState(Integer page, Integer limit);

    Map<String, Object> getDashboardData();

    boolean checkAccess(Long claimId);

    ClaimsData getClaimData(Long claimId);

    Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO);
}
