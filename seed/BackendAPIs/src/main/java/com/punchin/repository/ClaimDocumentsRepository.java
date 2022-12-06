package com.punchin.repository;

import com.punchin.entity.ClaimDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ClaimDocumentsRepository extends JpaRepository<ClaimDocuments, Long> {
    @Query(nativeQuery = true, value = " select * from claim_documents cd where cd.claims_data_id=:claimId ")
    List<Map<String, Object>> getAllClaimDocument(Long claimId);
}
