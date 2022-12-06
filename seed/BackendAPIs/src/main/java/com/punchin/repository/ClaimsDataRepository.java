package com.punchin.repository;

import com.punchin.dto.ClaimDataResponse;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimsDataRepository extends JpaRepository<ClaimsData, Long> {

    Page findByClaimStatus(ClaimStatus claimStatus, Pageable pageable);

    Page findByClaimStatusAndIsForwardToVerifier(ClaimStatus claimStatus, boolean isForwardToVerifier, Pageable pageable);

    Long countByClaimStatus(ClaimStatus inProgress);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claim_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claim_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND ca.user_id =:userId AND ca.is_active = true")
    Page findAllByAgentAllocated(User userId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claim_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claim_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND cd.claim_status =:claimStatus AND ca.user_id =:userId AND ca.is_active = true")
    Page findAllByAgentAllocatedAndClaimStatus(User userId, ClaimStatus claimStatus, Pageable pageable);
}
