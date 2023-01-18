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


    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.punchin_claim_id Ilike %:searchedKeyword% and cd.banker_id IN(:bankerIds) ORDER BY cd.punchin_claim_id")
    Page<ClaimsData> findAllBankerClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, List<Long> bankerIds, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.loan_account_number Ilike %:searchedKeyword% and cd.banker_id=(:bankerIds) ORDER BY cd.loan_account_number ")
    Page<ClaimsData> findAllBankerClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, List<Long> bankerIds, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where (cd.borrower_name Ilike %:searchedKeyword%) and cd.banker_id=(:bankerIds) ORDER BY cd.borrower_name ")
    Page<ClaimsData> findAllBankerClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, List<Long> bankerIds, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.punchin_claim_id Ilike %:searchedKeyword% and cd.banker_id=:bankerId ")
    Page<ClaimsData> findBankerClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.loan_account_number Ilike %:searchedKeyword% and cd.banker_id=:bankerId ")
    Page<ClaimsData> findBankerClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and (cd.borrower_name Ilike %:searchedKeyword%) and cd.banker_id=:bankerId ")
    Page<ClaimsData> findBankerClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, Long bankerId, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.punchin_claim_id Ilike %:searchedKeyword% ")
    List<ClaimsData> findVerifierClaimSearchedDataByClaimDataId1(@Param("searchedKeyword") String searchedKeyword);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.borrower_state=:state and cd.punchin_claim_id Ilike %:searchedKeyword%  ")
    Page<ClaimsData> findVerifierClaimSearchedDataByClaimDataId(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.borrower_state=:state and cd.loan_account_number Ilike %:searchedKeyword%  ")
    Page<ClaimsData> findVerifierClaimSearchedDataByLoanAccountNumber(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.claim_status in (:claimStatus) and cd.borrower_state=:state and (cd.borrower_name Ilike %:searchedKeyword%)  ")
    Page<ClaimsData> findVerifierClaimSearchedDataBySearchName(@Param("searchedKeyword") String searchedKeyword, List<String> claimStatus, String state, Pageable pageable);


    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocated(Long userId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE cd.is_deleted = false AND cd.claim_status IN(:claimStatus) AND ca.user_id =:userId AND ca.is_active = true")
    Page<ClaimsData> findAllByAgentAllocatedAndClaimStatus(Long userId, List<String> claimStatus, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT cd.* FROM claims_data AS cd INNER JOIN claim_allocated AS ca ON cd.id = ca.claims_data_id WHERE ca.claims_data_id =:claimId AND cd.is_deleted = false AND ca.user_id =:userId AND ca.is_active = true")
    ClaimsData findByIdAndUserId(Long claimId, Long userId);

    ClaimsData findByIdAndPunchinBankerId(Long claimId, String userId);

    Long countByBankerIdIn(List<Long> bankerIds);

    Long countByClaimStatusInAndBankerIdIn(List<ClaimStatus> claimsStatus, List<Long> bankerIds);

    Page<ClaimsData> findAllByBankerIdInOrderByCreatedAtDesc(List<Long> bankerIds, Pageable pageable);

    Page findByIsForwardToVerifierAndPunchinBankerId(boolean b, String userId, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInAndBankerIdInOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, List<Long> bankerIds, Pageable pageable);

    Long countByClaimStatusInAndBorrowerStateIgnoreCase(List<ClaimStatus> claimsStatus, String state);

    Long countByClaimStatusInAndVerifierId(List<ClaimStatus> claimsStatus, long id);

    Page<ClaimsData> findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, String state, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInAndVerifierIdOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, Long id, Pageable pageable);

    List<ClaimsData> findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, String state);

    List<ClaimsData> findByClaimStatusInAndVerifierIdOrderByClaimInwardDateDesc(List<ClaimStatus> claimsStatus, Long id);

    @Query(nativeQuery = true, value = "SELECT loan_account_number FROM claims_data WHERE id=:claimId")
    String findPunchinClaimIdById(Long claimId);

    Page findByClaimStatus(ClaimStatus underVerification, Pageable pageable);

    List<ClaimsData> findByClaimStatus(ClaimStatus claimStatus);


    Long countByAgentId(Long id);

    Long countByClaimStatusInAndAgentId(List<ClaimStatus> statusList, Long id);

    boolean existsByIdAndAgentId(Long claimId, Long id);

    Long countByBorrowerState(String state);

    Long countByVerifierId(Long id);

    Page<ClaimsData> findByBorrowerStateOrderByCreatedAtDesc(String state, Pageable pageable);

    Page<ClaimsData> findByVerifierIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    List<ClaimsData> findByVerifierIdOrderByClaimInwardDateDesc(Long id);

    Page<ClaimsData> findByAgentIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(List<ClaimStatus> statusList, Long id, Pageable pageable);

    ClaimsData findByIdAndBorrowerState(Long claimId, String state);

    ClaimsData findByIdAndVerifierId(Long claimId, Long id);

    List<ClaimsData> findAllByBankerIdInOrderByClaimInwardDateDesc(List<Long> bankerIds);

    List<ClaimsData> findByClaimStatusInAndBankerIdInOrderByClaimInwardDateDesc(List<ClaimStatus> claimsStatus, List<Long> bankerIds);

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

    Page findByClaimStatusAndVerifierId(ClaimStatus underVerification, Long id, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.borrower_state=:state and cd.punchin_claim_id Ilike %:searchedKeyword% ORDER BY cd.punchin_claim_id ")
    Page<ClaimsData> findAllVerifierClaimSearchedDataByClaimDataId(String searchedKeyword, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.verifier_id=:id and cd.punchin_claim_id Ilike %:searchedKeyword% ORDER BY cd.punchin_claim_id ")
    Page<ClaimsData> findAllVerifierClaimSearchedDataByClaimDataId(String searchedKeyword, Long id, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.borrower_state=:state and cd.loan_account_number Ilike %:searchedKeyword% ORDER BY cd.loan_account_number")
    Page<ClaimsData> findAllVerifierClaimSearchedDataByLoanAccountNumber(String searchedKeyword, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.verifier_id=:id and cd.loan_account_number Ilike %:searchedKeyword% ORDER BY cd.loan_account_number")
    Page<ClaimsData> findAllVerifierClaimSearchedDataByLoanAccountNumber(String searchedKeyword, Long id, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.borrower_state=:state and (cd.borrower_name Ilike %:searchedKeyword%) ORDER BY cd.borrower_name")
    Page<ClaimsData> findAllVerifierClaimDataBySearchName(String searchedKeyword, String state, Pageable pageable);

    @Query(nativeQuery = true, value = "select * from claims_data cd where cd.verifier_id=:id and (cd.borrower_name Ilike %:searchedKeyword%) ORDER BY cd.borrower_name")
    Page<ClaimsData> findAllVerifierClaimDataBySearchName(String searchedKeyword, Long id, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInOrClaimBankerStatusInAndPunchinBankerIdOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, List<ClaimStatus> claimsStatus1, String userId, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInOrClaimBankerStatusInAndVerifierIdOrderByCreatedAtDesc(List<ClaimStatus> claimsStatus, List<ClaimStatus> claimsStatus1, Long id, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM claims_data WHERE punchin_banker_id=:userId AND submitted_by is not null")
    Page<ClaimsData> findBySubmittedClaims(String userId, Pageable pageable);

    String findClaimStatusById(Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM claims_data WHERE id IN (SELECT DISTINCT claims_data_id FROM claim_documents WHERE is_active = true AND upload_side_by = 'banker') AND banker_id=(:bankerIds) AND submitted_by is null")
    Page<ClaimsData> findByClaimStatusByDraftSavedByBanker(List<Long> bankerIds, Pageable pageable);
    @Query(nativeQuery = true, value = "select * from  claims_data cd where cd.banker_id =:bankerId and cd.loan_account_number =:loanAccountNumber")
    List<Long> findExistingLoanNumber(@Param("bankerId") Long bankerId, @Param("loanAccountNumber") String loanAccountNumber);

    @Query(nativeQuery = true, value = "SELECT * FROM claims_data WHERE LOWER(punchin_claim_id)=:id")
    ClaimsData findIdByPunchinId(String id);

    Page<ClaimsData> findByClaimStatusInOrClaimBankerStatusInAndBankerIdIn(List<ClaimStatus> claimsStatus, List<ClaimStatus> claimsStatus1, List<Long> bankerIds, Pageable pageable);

    @Query(nativeQuery = true, value = " select * from claims_data cd where (cd.claim_banker_status in ('VERIFIER_DISCREPENCY','BANKER_DISCREPANCY', " +
            " 'NEW_REQUIREMENT') or cd.claim_status in ('VERIFIER_DISCREPENCY','BANKER_DISCREPANCY','NEW_REQUIREMENT')) and cd.verifier_id =:verifierId ORDER BY cd.created_at DESC ")
    Page<ClaimsData> findByClaimStatusOrClaimBankerStatusInAndVerifierId(@Param("verifierId") long verifierId, Pageable pageable);

    Page<ClaimsData> findByClaimStatusInOrClaimBankerStatusInAndVerifierId(List<ClaimStatus> claimsStatus, List<ClaimStatus> claimsStatus1, Long verifierId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM claims_data WHERE banker_id=:bankerId AND submitted_by is not null ORDER BY id DESC")
    Page<ClaimsData> findClaimPendingForBakerDocument(Long bankerId, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM claims_data WHERE banker_id=:bankerId AND submitted_by is null ORDER BY id DESC")
    Page<ClaimsData> findClaimPendingForBakerDocumentPending(Long bankerId, Pageable pageable);
}

