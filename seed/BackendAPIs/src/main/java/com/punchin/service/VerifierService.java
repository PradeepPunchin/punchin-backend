package com.punchin.service;

import com.punchin.dto.PageDTO;
import com.punchin.dto.VerifierClaimDataResponseDTO;
import com.punchin.dto.VerifierDashboardCountDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;

import java.util.List;
import java.util.Map;

public interface VerifierService {

    PageDTO getAllClaimsData(ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize);

    List<VerifierClaimDataResponseDTO> getDataClaimsData(Integer pageNo, Integer pageSize);

    Map<String, Long> getDashboardData();

    ClaimsData getClaimData(Long claimId);

    boolean allocateClaimToAgent(ClaimsData claimsData, User user);
}
