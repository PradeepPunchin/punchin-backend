


package com.punchin.service;

import com.punchin.dto.PageDTO;
import com.punchin.enums.ClaimStatus;

public interface VerifierService {

    PageDTO getAllClaimsData(ClaimStatus claimStatus, Integer pageNo, Integer pageSize);

    PageDTO getDataClaimsData(ClaimStatus claimStatus, Integer pageNo, Integer pageSize);

}
