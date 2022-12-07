package com.punchin.repository;

import com.punchin.entity.ClaimAllocated;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimAllocatedRepository extends JpaRepository<ClaimAllocated, Long> {

    @Query(nativeQuery = true, value = "SELECT count(*) FROM claim_allocated WHERE user_id =:userId")
    Long countByAllocatedToAgent(Long userId);

    @Query(nativeQuery = true, value = "SELECT count(ca.*) FROM claim_allocated as ca INNER JOIN claims_data as cd ON ca.claim_data_id = cd.id WHERE cd.claim_status=:claimStatus AND ca.user_id =:userId")
    Long countByClaimStatusByAgent(ClaimStatus claimStatus, Long userId);

    boolean existsByUserIdAndClaimsDataId(Long userId, Long claimId);
}
