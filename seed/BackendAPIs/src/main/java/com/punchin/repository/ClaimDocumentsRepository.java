package com.punchin.repository;

import com.punchin.entity.ClaimDocuments;
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

    @Query(nativeQuery = true, value = " select * from claim_documents cd where cd.doc_type in ('SINGNED_CLAIM_FORM', 'DEATH_CERTIFICATE', 'BORROWER_ID_PROOF', 'BORROWER_ADDRESS_PROOF',\n" +
            "                'NOMINEE_ID_PROOF', 'NOMINEE_ADDRESS_PROOF', 'BANK_ACCOUNT_PROOF', 'FIR_POSTMORTEM_REPORT', 'AFFIDAVIT', 'DISCREPANCY') cd.claim_data_id=:claimDataId ")
    List<ClaimDocuments> findClaimDocumentsByClaimDataId(@Param("claimDataId") Long claimDataId);
}
