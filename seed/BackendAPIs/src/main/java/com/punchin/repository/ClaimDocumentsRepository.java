package com.punchin.repository;

import com.punchin.entity.ClaimDocuments;
import com.punchin.enums.AgentDocType;
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

    ClaimDocuments findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(Long claimId, AgentDocType docType, String agent, boolean b, boolean b1);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByOrderById(Long id, String agent);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id =:id AND upload_side_by ='agent' AND is_active = true ORDER BY agent_doc_type")
    List<ClaimDocuments> getClaimDocumentWithDiscrepancyStatus(Long id);
    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id =:id AND upload_side_by ='banker' AND is_active = true ORDER BY agent_doc_type")
    List<ClaimDocuments> getClaimDocumentWithDiscrepancyStatusAndBanker(Long id);

    List<ClaimDocuments> findByClaimsDataIdAndAgentDocType(Long claimId, AgentDocType valueOf);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id =:id AND upload_side_by Ilike %:agent% AND is_active=:b")
    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(Long id, String agent, boolean b);

    boolean existsByClaimsDataIdAndUploadSideByAndIsActiveAndIsApproved(Long claimId, String agent, boolean b, boolean b1);

    List<ClaimDocuments> findByClaimsDataId(Long id);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsActive(Long id, String banker, boolean b);

    @Query(nativeQuery = true, value = "select exists (select * from claim_documents cd where cd.claims_data_id =:claimId and cd.agent_doc_type =:agentDoc)")
    boolean findExistingDocument(@Param("claimId") Long id, String agentDoc);

    @Query(nativeQuery = true, value = "SELECT EXISTS (SELECT * FROM claim_documents WHERE claims_data_id=:id AND agent_doc_type=:docType AND is_deleted=false AND upload_side_by=:sideBy)")
    boolean existsByClaimsDataIdAndUploadSideByAndAgentDocType(Long id, String sideBy, String docType);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsDeletedOrderById(Long id, String banker, boolean b);
    @Query(nativeQuery = true, value = " select * from claim_documents cd where cd.upload_by='agent' and cd.claims_data_id:claimDataId and cd.doc_type:docType ")
    ClaimDocuments findClaimDocumentsByClaimDataIdAndDocType(Long claimDataId, String docType);

    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsActiveOrderById(Long id, String agent, boolean b);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE is_deleted = false AND is_active = false AND is_verified = true AND is_approved = false AND upload_side_by = 'New Requirement' AND claims_data_id=:id")
    List<ClaimDocuments> getAdditionalDocumentRequestClaims(Long id);

    boolean existsByClaimsDataIdAndUploadSideByAndIsVerifiedAndIsApprovedAndIsActive(Long claimId, String agent, boolean b, boolean b1, boolean b2);

    @Query(nativeQuery = true, value = "SELECT DISTINCT agent_doc_type FROM claim_documents WHERE claims_data_id=:id AND upload_side_by=:agent AND is_active=:b ORDER BY agent_doc_type")
    List<String> findDistinctByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(Long id, String agent, boolean b);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id=:id AND upload_side_by=:agent AND is_active=:b AND agent_doc_type=:docType LIMIT 1")
    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsActiveAndAgentDocTypeOrderByAgentDocTypeLimit(Long id, String agent, boolean b, String docType);

    @Query(nativeQuery = true, value = "SELECT * FROM claim_documents WHERE claims_data_id=:id AND upload_side_by=:agent AND is_active=:b AND agent_doc_type=:docType")
    List<ClaimDocuments> findByClaimsDataIdAndUploadSideByAndIsActiveAndAgentDocTypeOrderByAgentDocType(Long id, String agent, boolean b, String docType);

    ClaimDocuments findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByOrderByIdDesc(Long claimId, AgentDocType docType, String newRequirement);

    List<ClaimDocuments> findByClaimsDataIdAndAgentDocTypeAndUploadSideByOrderByIdDesc(Long claimId, AgentDocType docType, String newRequirement);
}
