package com.punchin.repository;

import com.punchin.entity.ClaimsDataAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimsDataAuditRepository extends JpaRepository<ClaimsDataAudit, Long> {
}
