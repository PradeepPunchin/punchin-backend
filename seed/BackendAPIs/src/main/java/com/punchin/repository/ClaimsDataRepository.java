package com.punchin.repository;

import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimsDataRepository extends JpaRepository<ClaimsData, Long> {

    Page findByClaimStatus(ClaimStatus claimStatus, Pageable pageable);

    Page findByClaimStatusAndIsForwardToVerifier(ClaimStatus claimStatus, boolean isForwardToVerifier, Pageable pageable);

    Long countByClaimStatus(ClaimStatus inProgress);
}
