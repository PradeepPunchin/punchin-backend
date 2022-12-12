package com.punchin.repository;

import com.punchin.dto.ClaimDataResponse;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimsDataRepository extends JpaRepository<ClaimsData, Long> {

    Page<ClaimsData> findByClaimStatus(ClaimStatus claimStatus, Pageable pageable);

    Page<ClaimsData> findByClaimStatusAndIsForwardToVerifier(ClaimStatus claimStatus, boolean isForwardToVerifier, Pageable pageable);

    Long countByClaimStatus(ClaimStatus inProgress);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocated(Long userId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.claim_status IN (:claimStatus) AND cd.is_deleted = false AND cd.is_forward_to_verifier = true AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocated(List<String> claimStatus, Long userId, Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claims_data cd where cd.claim_status=:claimStatus ")
    Page<ClaimsData> findClaimDataByStatus(String claimStatus, Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claims_data")
    Page<ClaimsData> findAllClaimData(Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claims_data cd where cd.claim_status=:claimStatus ")
    List<ClaimsData> findByClaimStatus(String claimStatus);

    @Query(nativeQuery = true, value = " select cd.id as id,cd.claim_inward_date as registrationDate,cd.borrower_name as borrowerName, " +
            " cd.nominee_name as nomineeName,cd.nominee_contact_number as nomineeContactNumber,cd.nominee_address as nomineeAddress from claims_data cd where cd.claim_status='UNDER_VERIFICATION' ")
    List<ClaimDataResponse> findClaimsDataVerifier(Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND cd.claim_status =:claimStatus AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocatedAndClaimStatus(Long userId, ClaimStatus claimStatus, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND cd.claim_status IN(:claimStatus) AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocatedAndClaimStatus(Long userId, List<String> claimStatus, Pageable pageable);

    ClaimsData findByIdAndIsForwardToVerifier(Long claimId, boolean forwardStatus);

    @Query(nativeQuery = true, value = "select distinct * from claims_data cd inner join users u on u.user_state=cd.borrower_state and u.user_id='agent' ")
    List<ClaimsData> getClaimsByAgentState(Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status= 'UNDER_VERIFICATION' and cd.is_forward_to_verifier= true and cd.id=:claimDataId ")
    ClaimsData findClaimDataForVerifier(@Param("claimDataId") Long claimDataId);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claim_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claim_data_id WHERE cd.is_deleted = false AND cd.is_forward_to_verifier = true AND cd.claim_status =:claimStatus AND ca.user_id =:userId AND ca.is_active = true")
    Page findAllByAgentAllocatedAndClaimStatus(User userId, ClaimStatus claimStatus, Pageable pageable);

    ClaimsData findByIdAndPunchinBankerId(Long claimId, String userId);

    Long countByClaimStatusIn(List<ClaimStatus> claimStatuses);
}
