package com.punchin.repository;

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

    Page<ClaimsData> findByClaimStatusAndIsForwardToVerifier(ClaimStatus claimStatus, boolean isForwardToVerifier, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and cd.punchin_claim_id Ilike %:searchedKeyword% and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataByClaimDataId1(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, Long agentId);
  @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and cd.loan_account_number Ilike %:searchedKeyword% and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataByClaimDataId2(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, Long agentId);
  @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and (cd.borrower_name Ilike %:searchedKeyword%) and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataByClaimDataId3(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, Long agentId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.punchin_claim_id Ilike %:searchedKeyword% and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, List<String> claimStatus, Long agentId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.loan_account_number Ilike %:searchedKeyword% and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, List<String> claimStatus, Long agentId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and (cd.borrower_name Ilike %:searchedKeyword%) and cd.agent_id=:agentId ")
    Page<ClaimsData> findClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, Pageable pageable, List<String> claimStatus, Long agentId);


    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and cd.punchin_claim_id Ilike %:searchedKeyword% and cd.banker_id=:bankerId ")
    List<ClaimsData> findBankerClaimSearchedDataByClaimDataId1(@Param("searchedKeyword") String searchedKeyword, Long bankerId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.punchin_claim_id Ilike %:searchedKeyword% and cd.banker_id=:bankerId ")
    List<ClaimsData> findBankerClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.loan_account_number Ilike %:searchedKeyword% and cd.banker_id=:bankerId ")
    List<ClaimsData> findBankerClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and (cd.borrower_name Ilike %:searchedKeyword%) and cd.banker_id=:bankerId ")
    List<ClaimsData> findBankerClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and cd.punchin_claim_id Ilike %:searchedKeyword% ")
    List<ClaimsData> findVerifierClaimSearchedDataByClaimDataId1(@Param("searchedKeyword") String searchedKeyword);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.punchin_claim_id Ilike %:searchedKeyword%  ")
    List<ClaimsData> findVerifierClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.loan_account_number Ilike %:searchedKeyword%  ")
    List<ClaimsData> findVerifierClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and (cd.borrower_name Ilike %:searchedKeyword%)  ")
    List<ClaimsData> findVerifierClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus);


    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocated(Long userId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND cd.claim_status IN(:claimStatus) AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocatedAndClaimStatus(Long userId, List<String> claimStatus, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE ca.claims_data_id =:claimId AND cd.is_deleted = false AND ca.user_id =:userId AND ca.is_active = true")
    ClaimsData findByIdAndUserId(Long claimId, Long userId);

    ClaimsData findByIdAndPunchinBankerId(Long claimId, String userId);

    Long countByPunchinBankerId(String userId);

    Long countByClaimStatusInAndPunchinBankerId(List<ClaimStatus> claimsStatus, String userId);

    Page findAllByPunchinBankerIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page findByIsForwardToVerifierAndPunchinBankerId(boolean b, String userId, Pageable pageable);

    Page findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, String userId, Pageable pageable);

    Long countByClaimStatusInAndBorrowerStateIgnoreCase(List<ClaimStatus> claimsStatus, String state);

    Page<ClaimsData> findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT punchin_claim_id FROM claims_data WHERE id=:claimId")
    String findPunchinClaimIdById(Long claimId);

    Page findByClaimStatus(ClaimStatus underVerification, Pageable pageable);

    List<ClaimsData> findByClaimStatus(ClaimStatus claimStatus);


    Long countByAgentId(Long id);

    Long countByClaimStatusInAndAgentId(List<ClaimStatus> statusList, Long id);

    boolean existsByIdAndAgentId(Long claimId, Long id);

    Long countByBorrowerState(String state);

    Page<ClaimsData> findByBorrowerStateOrderByCreatedAtDesc(String state, Pageable pageable);

    Page<ClaimsData> findByAgentIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(List<ClaimStatus> statusList, Long id, Pageable pageable);

    ClaimsData findByIdAndBorrowerState(Long claimId, String state);

    List<ClaimsData> findAllByPunchinBankerId(String userId);

    List<ClaimsData> findByClaimStatusInAndPunchinBankerId(List<ClaimStatus> claimsStatus, String userId);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and (CAST(cd.id AS varchar) Ilike %:searchedKeyword%) ")
    Page<ClaimsData> findClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and cd.loan_account_number Ilike %:searchedKeyword% ")
    Page<ClaimsData> findClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in ('IN_PROGRESS','VERIFIER_DISCREPENCY','AGENT_ALLOCATED', " +
            "'ACTION_PENDING','CLAIM_SUBMITTED','CLAIM_INTIMATED','UNDER_VERIFICATION') and (cd.borrower_name Ilike %:searchedKeyword% or cd.nominee_name Ilike %:searchedKeyword%) ")
    Page<ClaimsData> findClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, Pageable pageable);

    Page findByClaimStatusAndBorrowerStateIgnoreCase(ClaimStatus underVerification, String state, Pageable pageable);
}
