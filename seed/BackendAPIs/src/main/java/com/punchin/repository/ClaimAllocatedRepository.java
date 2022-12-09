package com.punchin.repository;

import com.punchin.entity.ClaimAllocated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ClaimAllocatedRepository extends JpaRepository<ClaimAllocated, Long> {

    @Query(nativeQuery = true, value = "SELECT count(*) FROM claim_allocated WHERE user_id =:userId")
    Long countByAllocatedToAgent(Long userId);

    @Query(nativeQuery = true, value = "SELECT count(ca.*) FROM claim_allocated as ca INNER JOIN claims_data as cd ON ca.claims_data_id = cd.id WHERE cd.claim_status IN (:claimStatus) AND ca.user_id =:userId AND cd.is_forward_to_verifier = true")
    int countByClaimStatusByAgent(List<String> claimStatus, Long userId);

    boolean existsByUserIdAndClaimsDataId(Long userId, Long claimId);

    @Query(nativeQuery = true, value = "SELECT created_at FROM claim_allocated WHERE claims_data_id =:claimId AND user_id =:userId")
    Long getAllocationDate(Long claimId, Long userId);
}
