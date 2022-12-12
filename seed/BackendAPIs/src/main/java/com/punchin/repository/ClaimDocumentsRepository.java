package com.punchin.repository;

import com.punchin.entity.ClaimDocuments;
import com.punchin.enums.BankerDocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ClaimDocumentsRepository extends JpaRepository<ClaimDocuments, Long> {
    @Query(nativeQuery = true, value = " select * from claim_documents cd where cd.claims_data_id=:claimId ")
    List<Map<String, Object>> getAllClaimDocument(Long claimId);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideBy(Long claimId, String banker);

    @Query(nativeQuery = true, value = " select * from claim_documents cd where cd.upload_by='agent' and cd.claims_data_id:claimDataId ")
    List<ClaimDocuments> findClaimDocumentsByClaimDataId(@Param("claimDataId") Long claimDataId);

    @Query(nativeQuery = true, value = "select count(cd.id) from claim_documents cd where cd.upload_by='agent' and cd.claims_data_id=:claimDataId ")
    Long findApprovedClaimDocumentsByClaimDataId(@Param("claimDataId") Long claimDataId);

    boolean existsByClaimsDataIdAndUploadSideByAndIsVerified(Long claimId, String sideBy, boolean isVerified);

    ClaimDocuments findFirstByClaimsDataIdAndDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(Long claimId, String docType, String agent, boolean b, boolean b1);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByOrderById(Long id, String agent);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id =:id")
    List<ClaimDocuments> getClaimDocumentWithDiscrepancyStatus(Long id);
}
