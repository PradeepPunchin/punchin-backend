package com.punchin.repository;

import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimsDataRepository extends JpaRepository<ClaimsData, Integer> {

    Page findByClaimStatus(String claimStatus, Pageable pageable);

    ClaimsData findByClaimStatus(String claimStatus);
}
