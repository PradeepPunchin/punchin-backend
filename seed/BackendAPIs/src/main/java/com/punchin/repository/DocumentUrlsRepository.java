package com.punchin.repository;

import com.punchin.entity.DocumentUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentUrlsRepository extends JpaRepository<DocumentUrls, Long> {

    @Query(nativeQuery = true, value = " select * from document_urls du inner join claim_documents_document_urls cddu ON du.id =cddu.document_urls_id " +
            " where cddu.claim_documents_id=:claimDocumentId")
    List<DocumentUrls> findDocumentUrlsByClaimDocumentId(@Param("claimDocumentId") Long claimDocumentId);

    @Query(nativeQuery = true, value = "SELECT * FROM document_urls du INNER JOIN claim_documents_document_urls cddu ON du.id = cddu.document_urls_id INNER JOIN claim_documents cd ON cddu.claim_documents_id = cd.id WHERE cd.claims_data_id=:id AND cd.upload_side_by=:agent AND cd.is_active=:b AND cd.agent_doc_type=:docType")
    List<DocumentUrls> findDocumentUrlsByClaimDocument(Long id, String agent, boolean b, String docType);

    @Query(nativeQuery = true, value = "select * from document_urls du inner join claim_documents_document_urls cddu ON du.id =cddu.document_urls_id where cddu.claim_documents_id in (SELECT id FROM claim_documents WHERE claims_data_id=:id AND upload_side_by=:agent AND is_active=:b AND agent_doc_type=:docType)")
    List<DocumentUrls> findAllDocumentAccordingToClaimDocuments(Long id, String agent, boolean b, String docType);
}
