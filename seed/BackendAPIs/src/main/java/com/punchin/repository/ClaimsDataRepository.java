package com.punchin.repository;

import com.punchin.dto.ClaimDataResponse;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimsDataRepository extends JpaRepository<ClaimsData, Long> {

    Page findByClaimStatus(ClaimStatus claimStatus, Pageable pageable);

    Page findByClaimStatusAndIsForwardToVerifier(ClaimStatus claimStatus, boolean isForwardToVerifier, Pageable pageable);

    Long countByClaimStatus(ClaimStatus inProgress);

    @Query(nativeQuery = true, value = " select * from claims_data cd where cd.claim_status=:claimStatus ")
    Page<ClaimsData> findClaimDataByStatus(String claimStatus, Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claim_data")
    Page<ClaimsData> findAllClaimData(Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claims_data cd where cd.claim_status=:claimStatus ")
    List<ClaimsData> findByClaimStatus(String claimStatus);

    @Query(nativeQuery = true, value = " select cd.id as id,cd.claim_inward_date as registrationDate,cd.borrower_name as borrowerName, " +
            " cd.nominee_name as nomineeName,cd.nominee_contact_number as nomineeContactNumber,cd.nominee_address as nomineeAddress from claims_data cd where cd.claim_status='UNDER_VERIFICATION' ")
    List<ClaimDataResponse> findClaimsDataVerifier(Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status= 'UNDER_VERIFICATION' and cd.id=:claimDataId ")
    ClaimsData findClaimDataForVerifier(@Param("claimDataId") Long claimDataId);
}
