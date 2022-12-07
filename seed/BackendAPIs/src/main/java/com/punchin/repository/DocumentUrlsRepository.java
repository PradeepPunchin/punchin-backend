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
}
