package com.punchin.repository;

import com.punchin.entity.ClaimsRemarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimsRemarksRepository extends JpaRepository<ClaimsRemarks, Long> {
    List<ClaimsRemarks> findByClaimIdOrderById(Long id);
}
