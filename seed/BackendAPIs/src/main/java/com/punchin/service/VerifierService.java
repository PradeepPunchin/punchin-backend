


package com.punchin.service;

import com.punchin.dto.PageDTO;
import com.punchin.dto.VerifierClaimDataResponseDTO;
import com.punchin.dto.VerifierDashboardCountDTO;
import com.punchin.enums.ClaimStatus;

import java.util.List;

public interface VerifierService {

    PageDTO getAllClaimsData(ClaimStatus claimStatus, Integer pageNo, Integer pageSize);

    List<VerifierClaimDataResponseDTO> getDataClaimsData(Integer pageNo, Integer pageSize);

    VerifierDashboardCountDTO getDashboardDataCount();
}
