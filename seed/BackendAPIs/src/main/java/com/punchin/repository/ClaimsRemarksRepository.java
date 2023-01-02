package com.punchin.repository;

import com.punchin.entity.ClaimsRemarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimsRemarksRepository extends JpaRepository<ClaimsRemarks, Long> {
}
